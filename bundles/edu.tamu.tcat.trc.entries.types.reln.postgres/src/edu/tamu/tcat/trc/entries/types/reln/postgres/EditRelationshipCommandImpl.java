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
package edu.tamu.tcat.trc.entries.types.reln.postgres;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.tamu.tcat.trc.persist.IdFactory;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.dto.AnchorDTO;
import edu.tamu.tcat.trc.entries.types.reln.dto.ProvenanceDTO;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;
import edu.tamu.tcat.trc.entries.types.reln.internal.dto.BasicAnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;


public class EditRelationshipCommandImpl implements EditRelationshipCommand
{
   private final RelationshipDTO relationship;

   private Function<RelationshipDTO, Future<String>> commitHook;

   public EditRelationshipCommandImpl(RelationshipDTO relationship, IdFactory idFactory)
   {
      this.relationship = relationship;
   }

   public void setCommitHook(Function<RelationshipDTO, Future<String>> hook)
   {
      commitHook = hook;
   }

   @Override
   public void setAll(RelationshipDTO relationship)
   {
       setTypeId(relationship.typeId);
       setDescription(relationship.description);
       setDescriptionFormat(relationship.descriptionMimeType);
       setProvenance(relationship.provenance);
       setTargetEntities(createAnchorSet(relationship.targetEntities));
       setRelatedEntities(createAnchorSet(relationship.relatedEntities));
   }

   private static BasicAnchorSet createAnchorSet(Set<AnchorDTO> entities)
   {
      if (entities.isEmpty())
         return new BasicAnchorSet(new HashSet<>());

      Set<Anchor> anchors = new HashSet<>();
      for (AnchorDTO anchorData : entities)
      {
         anchors.add(AnchorDTO.instantiate(anchorData));
      }

      return new BasicAnchorSet(anchors);
   }

   @Override
   public void setTypeId(String typeId)
   {
      relationship.typeId = typeId;
   }

   @Override
   public void setType(RelationshipType typeRelationship)
   {
      // TODO
   }

   @Override
   public void setDescription(String description)
   {
      relationship.description = description;
   }

   @Override
   public void setDescriptionFormat(String descriptionFormat)
   {
      relationship.descriptionMimeType = descriptionFormat;
   }

   @Override
   public void setProvenance(ProvenanceDTO provenance)
   {
      relationship.provenance = provenance;
   }

   @Override
   public void setRelatedEntities(AnchorSet related)
   {
      if (related == null)
         return;

      relationship.relatedEntities = related.getAnchors().parallelStream()
                       .map(anchor -> AnchorDTO.create(anchor))
                       .collect(Collectors.toSet());
   }

   @Override
   public void addRelatedEntity(AnchorDTO anchor)
   {
      relationship.relatedEntities.add(anchor);
   }

   @Override
   public void addRelatedEntities(Set<AnchorDTO> anchor)
   {
      relationship.relatedEntities.addAll(anchor);
   }

   @Override
   public void removeRelatedEntity(AnchorDTO anchor)
   {
      relationship.relatedEntities.remove(anchor);
   }

   @Override
   public void setTargetEntities(AnchorSet target)
   {
      if (target == null)
         return;

      relationship.targetEntities = target.getAnchors().parallelStream()
            .map(anchor -> AnchorDTO.create(anchor))
            .collect(Collectors.toSet());
   }

   @Override
   public void addTargetEntity(AnchorDTO anchor)
   {
      relationship.targetEntities.add(anchor);
   }

   @Override
   public void addTargetEntities(Set<AnchorDTO> anchor)
   {
      relationship.targetEntities.addAll(anchor);
   }

   @Override
   public void removeTargetEntity(AnchorDTO anchor)
   {
      relationship.targetEntities.remove(anchor);
   }

   @Override
   public Future<String> execute()
   {
      Objects.requireNonNull(commitHook, "");

      return commitHook.apply(relationship);
   }

}
