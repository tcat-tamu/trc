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

import edu.tamu.tcat.trc.entries.core.repo.EditEntryCommand;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipException;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.resolver.EntryId;

/**
 * An edit command to modify a {@link Relationship}.
 */
public interface EditRelationshipCommand extends EditEntryCommand<Relationship>
{
   /**
    * Set the {@link RelationshipType} of the {@link Relationship}.
    * @param typeRelationship
    */
   void setType(RelationshipType typeRelationship);

   /**
    * @param typeId The string identifier of the relationship type. Must correspond to the
    *    to a valid relationship type within the configured {@link RelationshipTypeRegistry}.
    */
   void setTypeId(String typeId);

   /**
    * Sets an editorial description of this relationship. Expected to be in lightly marked HTML.
    */
   void setDescription(String description);

   /**
    * Constructs a mutator that can be used to modify the anchor associated with the referenced
    * entry. Note that this is the source entry set of a directed relationship type. If an anchor
    * for the referenced entry does not exist, this method will create one.
    *
    * @return A mutator to edit the associated anchor.
    */
   AnchorMutator editRelatedEntry(EntryId ref);

   /**
    * Removes the anchor associated with the referenced entry. If no anchor is associated
    * with the supplied reference (at the time of execution), no action will be performed.
    */
   void removeRelatedEntry(EntryId ref);

   /**
    * Removes all anchors from the set of related entries.
    */
   void clearRelatedEntries();

   /**
    * Constructs a mutator that can be used to modify the anchor associated with the referenced
    * entry. If an anchor for the referenced entry does not exist, this method will create one.
    *
    * @return A mutator to edit the associated anchor.
    * @throws RelationshipException If this relationship is undirected.
    */
   AnchorMutator editTargetEntry(EntryId ref);

   /**
    * Removes the anchor associated with the referenced entry. If no anchor is associated
    * with the supplied reference (at the time of execution), no action will be performed.
    */
   void removeTargetEntry(EntryId ref);

   /**
    * Removes all anchors from the set of target entries. Has no effect for undirected relationships.
    */
   void clearTargetEntries();

}
