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
package
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype;

import javax.persistence.*;

@Entity
@SqlResultSetMapping(name = "EmbedMapping", entities = {
@EntityResult(entityClass = EmbedOwner.class, fields = {
@FieldResult(name = "pk", column = "OWNER_PK"),
@FieldResult(name = "basic", column = "OWNER_BASIC"),
@FieldResult(name = "embed.basic", column = "EMBED_BASIC"),
@FieldResult(name = "embed.clob", column = "EMBED_CLOB")
    })
    })
public class EmbedOwner {

    @Embedded
    @AttributeOverride(name = "basic", column = @Column(name = "OVER_BASIC"))
    @AssociationOverride(name = "owner",
        joinColumns = @JoinColumn(name = "OVER_OWNER"))
    protected EmbedValue embed;

    @Basic
    @Column(name = "OWN_BASIC")
    protected String basic;

    @Id
    @GeneratedValue
    protected int pk;

    public int getPk() {
        return pk;
    }

    public void setBasic(String basic) {
        this.basic = basic;
    }

    public String getBasic() {
        return basic;
    }

    public void setEmbed(EmbedValue embed) {
        this.embed = embed;
    }

    public EmbedValue getEmbed() {
        return embed;
    }
}
