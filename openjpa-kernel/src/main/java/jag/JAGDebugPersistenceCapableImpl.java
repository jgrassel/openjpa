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

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.StateManager;
import org.apache.openjpa.kernel.DetachedStateManager;
import org.apache.openjpa.kernel.DetachedValueStateManager;
import org.apache.openjpa.kernel.ObjectIdStateManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;

public class JAGDebugPersistenceCapableImpl
    implements JAGDebugPersistenceCapable {

    @Override
    public String debug(Object pcObj) {
        if (!(pcObj instanceof PersistenceCapable)) {
            return "[[Not PersistenceCapable]]";
        }
        
        final StringBuilder sb = new StringBuilder();
        
        try {
            sb.append("[[PersistentCapable: ");
            // Print Object Type and Address First
            sb.append(pcObj.getClass().getName());
            sb.append("@").append(Integer.toHexString(System.identityHashCode(pcObj)));
            
            // Separate type/address with persistence info
            sb.append(": ");
            
            final PersistenceCapable pc = (PersistenceCapable) pcObj;
            final StateManager sm = pc.pcGetStateManager();
            final OpenJPAStateManager ojpasm = (sm != null && sm instanceof OpenJPAStateManager) 
                    ? (OpenJPAStateManager) sm 
                    : null;
            
            sb.append("StateManager = ");
            if (sm != null) {
                sb.append(sm.getClass().getName());
                sb.append("@").append(Integer.toHexString(System.identityHashCode(sm)));
                sb.append(" -- ");
                sb.append(sm).append("; ");
            } else {
                sb.append(sm).append("; ");
            }
            
            
            if (ojpasm != null) {
                sb.append("id = ").append(ojpasm.getId()).append("; ");
                sb.append("object id = ").append(ojpasm.getObjectId()).append("; ");
                sb.append("version = ").append(ojpasm.getVersion()).append("; ");
                sb.append("isEmbedded = ").append(ojpasm.isEmbedded()).append("; ");
                
                if (ojpasm instanceof DetachedStateManager) {
                    // Many getter methods will throw UnsupportedOperationException...
                    DetachedStateManager dsm = (DetachedStateManager) ojpasm;
                    sb.append("isFlushed = NA; ");
                    sb.append("isFlushedDirty = NA; ");
                    sb.append("owner = NA; ");
                    sb.append("PCState = NA; "); 
                } else if (ojpasm instanceof DetachedValueStateManager) {
                    // Some getter methods will throw UnsupportedOperationException...
                    sb.append("isFlushed = ").append(ojpasm.isFlushed()).append("; ");
                    sb.append("isFlushedDirty = ").append(ojpasm.isFlushedDirty()).append("; ");
                    sb.append("owner = ").append(ojpasm.getOwner()).append("; ");
                    sb.append("PCState = NA; "); 
                } else if (ojpasm instanceof ObjectIdStateManager) {
                    // Some getter methods will throw UnsupportedOperationException...
                    sb.append("isFlushed = ").append(ojpasm.isFlushed()).append("; ");
                    sb.append("isFlushedDirty = ").append(ojpasm.isFlushedDirty()).append("; ");
                    sb.append("owner = ").append(ojpasm.getOwner()).append("; ");
                    sb.append("PCState = NA; "); 
                } else {
                    sb.append("isFlushed = ").append(ojpasm.isFlushed()).append("; ");
                    sb.append("isFlushedDirty = ").append(ojpasm.isFlushedDirty()).append("; ");
                    sb.append("owner = ").append(ojpasm.getOwner()).append("; ");
                    sb.append("PCState = ").append(ojpasm.getPCState()).append("; "); 
                }
            }
            if (sm != null) {
                sb.append("isDirty = ").append(sm.isDirty()).append("; ");
                sb.append("isNew = ").append(sm.isNew()).append("; ");
                sb.append("isDeleted = ").append(sm.isDeleted()).append("; ");
                sb.append("isDetached = ").append(sm.isDetached()).append("; ");
            }
            
        } catch (Throwable t) {
            // Ouch
        } finally {
            sb.append("]]");
        }
        
        return sb.toString();
    }

}
