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
package edu.tamu.tcat.trc.entries.types.reln.repo;

import java.util.Set;

import edu.tamu.tcat.trc.entries.core.repo.EditEntryCommand;
import edu.tamu.tcat.trc.entries.types.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.dto.AnchorDTO;

/**
 * Gives the ability to edit a {@link Relationship}. This will allow the clients
 * to update a {@Relationship} between a set of {@Anchor}'s.
 */
public interface EditRelationshipCommand extends EditEntryCommand<Relationship>
{
   /**
    * Set the {@link RelationshipType} of the {@link Relationship}.
    * @param typeRelationship
    */
   void setType(RelationshipType typeRelationship);

   /**
    * Set the {@link RelationshipType.typeId} for the {@link Relationship}.
    * @param typeId
    */
   void setTypeId(String typeId);

   /**
    *
    * @param description
    */
   void setDescription(String description);

   /**
    *
    * @param descriptionFormat
    */
   void setDescriptionFormat(String descriptionFormat);

   /**
    *
    * @param related
    */
   void setRelatedEntities(AnchorSet related);

   /**
    * @param related
    */
   void addRelatedEntities(Set<AnchorDTO> related);

   /**
    *
    * @param anchor
    */
   void addRelatedEntity(AnchorDTO anchor);

   /**
    *
    * @param anchor
    */
   void removeRelatedEntity(AnchorDTO anchor);

   /**
    *
    * @param target
    */
   void setTargetEntities(AnchorSet target);

   /**
    * @param target
    */
   void addTargetEntities(Set<AnchorDTO> target);

   /**
    *
    * @param anchor
    */
   void addTargetEntity(AnchorDTO anchor);

   /**
    *
    * @param anchor
    */
   void removeTargetEntity(AnchorDTO anchor);

}
