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
package org.apache.openjpa.persistence.generationtype;

import javax.persistence.*;
import java.io.*;

/**
 * Extension of Animal class illustrating inheritance.
 */
@Entity(name = "Dog1")
@Table(name = "DOGTAB", schema = "SCHEMA1")
@IdClass(DogId.class)
public class Dog1 implements Serializable

{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id2;

    @Id
    private int datastoreid;

    private String name;

    private float price;

    private boolean domestic;

    public Dog1() {
        super();

    }

    public Dog1(String name) {
        this.id2 = id2;
        this.name = name;

    }

    public int getId2() {
        return id2;
    }

    public void setId2(int id) {
        this.id2 = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public boolean isDomestic() {
        return domestic;
    }

    public void setDomestic(boolean domestic) {
        this.domestic = domestic;
    }

    public int getDatastoreid() {
        return datastoreid;
    }

    public void setDatastoreid(int datastoreid) {
        this.datastoreid = datastoreid;
    }
}
