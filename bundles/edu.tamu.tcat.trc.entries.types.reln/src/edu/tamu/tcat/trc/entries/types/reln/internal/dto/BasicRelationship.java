/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.entries.types.reln.internal.dto;

import edu.tamu.tcat.trc.entries.types.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.Provenance;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;

public class BasicRelationship implements Relationship
{
   private final String id;
   private final RelationshipType type;
   private final String desc;
   private final String descType;
   private final Provenance prov;
   private final AnchorSet related;
   private final AnchorSet target;

   public BasicRelationship(String id, RelationshipType type,
                             String desc, String descType, Provenance prov,
                             AnchorSet related, AnchorSet target) {
      this.id = id;
      this.type = type;
      this.desc = desc;
      this.descType = descType;
      this.prov = prov;
      this.related = related;
      this.target = target;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public RelationshipType getType()
   {
      return type;
   }

   @Override
   public String getDescription()
   {
      return desc;
   }

   @Override
   public String getDescriptionFormat()
   {
      return descType;
   }

   @Override
   public Provenance getProvenance()
   {
      return prov;
   }

   @Override
   public AnchorSet getRelatedEntities()
   {
      return related;
   }

   @Override
   public AnchorSet getTargetEntities()
   {
      return target;
   }

}