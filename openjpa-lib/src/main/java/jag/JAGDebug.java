/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package jag;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

// START - ALLOW PRINT STATEMENTS

public class JAGDebug {
	private static final String PC_CLS = "org.apache.openjpa.enhance.PersistenceCapable";
	private static final int MAX_LIST_DUMP = 10;
	
	private static final JAGDebugPersistenceCapable pcDebugger;
	
	static {
	    JAGDebugPersistenceCapable instance = null;
	    try {
	        // Class exists in kernel, which openjpa-lib doesn't depend on.
	        Class pcDebuggerClass = Class.forName("jag.JAGDebugPersistenceCapableImpl");
	        instance = (JAGDebugPersistenceCapable) pcDebuggerClass.newInstance();	        
	    } catch (Throwable t) {
	        // oof.
	    }
	    pcDebugger = instance;
	}
	
    private static final ThreadLocal<ReportInfo> ri = new ThreadLocal<ReportInfo>() {
        @Override
        protected ReportInfo initialValue() {
            return new ReportInfo();
        }
    };
    
    public static final void beginReportTracking(boolean b) {
        beginReportTracking();
        if (b) {
            setPrintReport(true);
        }
    }
    
    final static String[] filter = { "^java\\..*", "^javax\\..*", "^org\\.apache\\..*", "^com\\.ibm\\..*",
            "^serp\\..*", "^org\\.\\.objectweb\\.asm\\..*", "^sun\\..*"};
    final static java.util.regex.Pattern[] patterns;
    static {
        patterns = new java.util.regex.Pattern[filter.length];
        for (int index = 0; index < filter.length; index++) {
            patterns[index] = java.util.regex.Pattern.compile(filter[index]);
        }
    }
    
    public static final void beginReportTracking() {
        try {
            final ReportInfo rii = ri.get();
            rii.counter.incrementAndGet();
            if (rii.counter.get() == 1) {
                try {
                    final StackTraceElement[] steArr = Thread.currentThread().getStackTrace();
                    StringBuilder sb = new StringBuilder();
                    sb.append("Inital Stack:");
                    boolean filteredlastThreadStack = false;
                    int max = 200;
                    int count = 0;
                    for (StackTraceElement ste : steArr) {
                        if (!filteredlastThreadStack) {
                            if (ste.toString().startsWith("java.lang.Thread") || ste.toString().startsWith("jag.JAGDebug")) {
                                continue;
                            }
                            filteredlastThreadStack = true;
                        }
                        
                        sb.append("\n").append(ste.toString());
                        
                        boolean match = false;
                        for (java.util.regex.Pattern pat : patterns) {
                            java.util.regex.Matcher m = pat.matcher(ste.toString());
                            if (m.matches()) {
                                match = true;
                            }
                        }
//                        if (!match) {
//                            break;
//                        }
                        
                        count++;
                        if (count >= max) {
                            break;
                        }
                    }
                    sb.append("\n");
                    report(sb.toString());
                } catch (Throwable t) {}
            }
        } catch (Throwable t) {
            // Exceptions in debug should never damage the product.
        }        
    }
    
    public static final void endReportTracking(boolean b) {
        if (b) {
            setPrintReport(true);
        }
        endReportTracking();
    }
    
    public static final void endReportTracking() {
        try {
            final ReportInfo rii = ri.get();
            if (rii.counter.decrementAndGet() <= 0) {
                // Last block involved in this report tracking.  Print the report, and remove the ThreadLocal instance.
                ri.remove();
                
                if (!rii.printReport.get()) {
                    // Do not print the report
                    return;
                }
                
                final StringBuilder sb = new StringBuilder();
                sb.append("--------------------");
                sb.append("\nJAG: (");
                final long ct = System.currentTimeMillis();
                final long nt = System.nanoTime();
                sb.append(provideFormattedTimestamp(ct));
                sb.append(") (").append(String.format("%,d", nt - rii.startTime)).append(" ns) ");
                
                try {
                    // Print thread info, which may run foul of Java2Sec.
                    final Thread cthread = Thread.currentThread();
                    sb.append(" (").append(cthread.getName()).append(" / ").append(cthread.getId()).append(") ");                
                } catch (Throwable t) {}
                
                sb.append("\n");
                sb.append(rii.reportSB.toString());
                sb.append("\n--------------------");
                System.out.println(sb);
            }
        } catch (Throwable t) {
            // Exceptions in debug should never damage the product.
        }
    }
    
    public static final boolean getPrintReport() {
        try {
            final ReportInfo rii = getReportInfo();
            if (rii != null) {
                return rii.printReport.get();
            } 
        } catch (Throwable t) {
            // Exceptions in debug should never damage the product.
        }
        
        return false;
    }
    public static final void setPrintReport(boolean b) {
        try {
            final ReportInfo rii = getReportInfo();
            if (rii != null) {
                rii.printReport.set(b);
            }
        } catch (Throwable t) {
            // Exceptions in debug should never damage the product.
        }        
    }
    
    public static final void report(String reportStr) {
        try {
            final ReportInfo rii = getReportInfo();
            if (rii != null) {
                final long ct = System.currentTimeMillis();
                final StringBuilder sb = new StringBuilder();
                sb.append("\n").append(provideFormattedTimestamp(ct));
                sb.append(": ");
                sb.append(reportStr);
                
                String indent = "";
                int indentLen = (rii.counter.get() - 1) *2;
                if (indentLen > 0) {
                    for (int index = 0; index < indentLen; index++) {
                        indent += " ";
                    }
                }
                
                String output = sb.toString();
                output = output.replaceAll("(?m)^", indent);
                
                rii.reportSB.append(output);
            }
        } catch (Throwable t) {
            // Exceptions in debug should never damage the product.
        }        
    }
    
    public static final ReportChain startReportChain() {
        try {
            final ReportInfo rii = getReportInfo();
            if (rii != null) {
                rii.reportEntrySB.setLength(0); // Wipe out anything old in the SB
            }
        } catch (Throwable t) {
            // Exceptions in debug should never damage the product.
        }
        
        return new ReportChain();
    }
    
    private static final void commitReportEntry() {
        try {
            final ReportInfo rii = getReportInfo();
            if (rii != null) {
                String str = rii.reportEntrySB.toString();
                rii.reportEntrySB.setLength(0);
                report(str);
            }
        } catch (Throwable t) {
            // Exceptions in debug should never damage the product.
        }
    }
    
    private static final void reportEntry(Object entry) {
        try {
            final ReportInfo rii = getReportInfo();
            if (rii != null) {
                rii.reportEntrySB.append(entry);
            }
        } catch (Throwable t) {
            // Exceptions in debug should never damage the product.
        }
    }
    
    private static final ReportInfo getReportInfo() {
        // This method doesn't catch Exceptions, so make sure it is always called within a catch-try block to protect the product
        final ReportInfo rii = ri.get();
        if (rii.counter.get() <= 0) {
            // Ignore if invoked out of scope of report tracking.
            ri.remove();
            return null;
        } else {
            return rii;
        }            
    }
    
    private static final String provideFormattedTimestamp(long tm) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(tm);
        
        final String tStr = "EEE, d MMM yyyy HH:mm:ss:SSS Z"; // "HH:mm:ss:SSS"
        final String timeString = new SimpleDateFormat(tStr).format(cal.getTime());
        return timeString;
        
//        long second = (tm / 1000) % 60;
//        long minute = (tm / (1000 * 60)) % 60;
//        long hour = (tm / (1000 * 60 * 60)) % 24;
//
//        final String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, (tm % 1000));
//        return time;
    }
    
    private static class ReportInfo {
        private final AtomicInteger counter = new AtomicInteger(0);
        private final AtomicBoolean printReport = new AtomicBoolean(false);
        private final StringBuffer reportSB = new StringBuffer();
        private final StringBuffer reportEntrySB = new StringBuffer();
        private final long startTime = System.nanoTime();
    }
    
    public static final class ReportChain {
        private boolean isDone = false;
        
        public ReportChain nl() {
        	if (!isDone) {
        		append("\n");
        	}
        	
        	return this;
        }
        
        public ReportChain logVariableAddress(String varName, Object o) {
        	if (!isDone) {
        		append(" ");
        		append(varName).append(" = ");
        		try {
        			if (o != null) {
    					append(" (").append(o.getClass().getName()).append("@");
    					append(Integer.toHexString(System.identityHashCode(o)));
    					append(")");
    				} else {
    					append("null");
    				}
        		} catch (Throwable t) {}
        	}
        	
        	return this;
        }
        
        public ReportChain logObjectAddress(Object o) {
            if (!isDone) {
                append(" ");
                try {
                    if (o != null) {
                        append("(").append("@");
                        append(Integer.toHexString(System.identityHashCode(o)));
                        append(")");
                    } else {
                        append("null");
                    }
                } catch (Throwable t) {}
            }
            
            return this;
        }
        
        private void logObjectInternal(Object o, boolean runToStringOnPCClass) {
        	if (o == null) {
        		append(o);
        		return;
        	}
        	
        	if (isObjectPersistenceCapable(o) && pcDebugger != null) {
        	    append(pcDebugger.debug(o));
        	    return;
        	} else if (!runToStringOnPCClass && isObjectPersistenceCapable(o)) {
        		append("<<Object is PersistenceCapable>>");
        		return;
        	}
        	
        	append(o);
        }
        
        private boolean isObjectPersistenceCapable(Object o) {
        	if (o == null) {
        		return false;
        	}
        	
        	final Class oClass = o.getClass();
    		final Class[] iFaces = oClass.getInterfaces();
    		
    		if (iFaces == null || iFaces.length == 0)
    			return false;
    		
    		for (Class c : iFaces) {
				if (PC_CLS.equalsIgnoreCase(c.getName())) {
					return true;
				}
			}
        	
        	return false;
        }
        
        private void logObjectAddrInternal(Object o) {
        	if (o == null) {
        		append(" (null) ");
        		return;
        	}
        	
        	boolean isPrimitive = o.getClass().isPrimitive();        	
        	try {
        		append(" (").append(o.getClass().getName()); 
        		if (isPrimitive) {
        			append(" (primitive) ");
        		} else {
        			append("@").append(Integer.toHexString(System.identityHashCode(o)));    				
        		}
        		append(")");
    		} catch (Throwable t) {}
        }
        
        public ReportChain logVariable(String varName, Object o) {
        	// Do not invoke PersistenceCapable toString() method, might invoke unexpected fetch
        	return logVariable(varName, o, false);
        }
        
        public ReportChain logVariable(String varName, Object o, boolean runToStringOnPCClass) {
        	if (!isDone) {
        		append(" ").append(varName).append(" = ");
    		
        		logObjectInternal(o, runToStringOnPCClass);
        		logObjectAddrInternal(o);
        		
        		try {
        			if (o != null && o instanceof Collection) {
            			final Collection<?> c = (Collection<?>) o;
            			if (c.isEmpty()) {
            				append(" { }");
            			} else {
            				append(" {").append(c.size()).append(": ");
            				int max = (MAX_LIST_DUMP > c.size() ? MAX_LIST_DUMP : c.size());
            				if (c.size() > 10) {
            					append("(listing only first ").append(MAX_LIST_DUMP).append(") ");
            				}
            				
            				boolean first = true;
            				for (Object obj : c) {
            					if (first) {
            						first = false;
            					} else {
            						append(", ");
            					}
            					logObjectInternal(obj, runToStringOnPCClass);
            	        		logObjectAddrInternal(obj);
            					
            					if (--max <= 0) {
            						break;
            					}
            				}
            				append(" }");
            			}           			
            		} else if (o != null && o.getClass().isArray()) {
            			Object[] objArr = (Object[]) o;
            			if (objArr.length == 0) {
            				append(" [ ]");
            			} else {
            				append(" [").append(objArr.length).append(": ");
            				int max = (MAX_LIST_DUMP > objArr.length ? MAX_LIST_DUMP : objArr.length);
            				boolean first = true;
            				for (int idx = 0; idx < max; idx++) {
            					if (first) {
            						first = false;
            					} else {
            						append(", ");
            					}
            					
            					logObjectInternal(objArr[idx], runToStringOnPCClass);
            	        		logObjectAddrInternal(objArr[idx]);
            				}
            				append(" ]");
            			}
            		}
        		} catch (Throwable t) {}
        		
        	}
        	
        	return this;
        }
        
        public ReportChain append(Object o) {
            if (!isDone) {
                reportEntry(o);
            }
            
            return this;
        }
        
        public void done() {
            if (!isDone) {
                commitReportEntry();
                isDone = true;
            }
        }
    }
}

// STOP - ALLOW PRINT STATEMENTS
