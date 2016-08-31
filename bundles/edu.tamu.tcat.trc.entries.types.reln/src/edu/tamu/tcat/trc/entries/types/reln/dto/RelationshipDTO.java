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
package edu.tamu.tcat.trc.entries.types.reln.dto;

import java.util.HashSet;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.Provenance;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.internal.dto.BasicAnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.internal.dto.BasicProvenance;
import edu.tamu.tcat.trc.entries.types.reln.internal.dto.BasicRelationship;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipException;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;

public class RelationshipDTO
{
   public String id;
   public String typeId;
   public String description;
   public String descriptionMimeType;
   public ProvenanceDTO provenance;
   public Set<AnchorDTO> relatedEntities = new HashSet<>();
   public Set<AnchorDTO> targetEntities = new HashSet<>();

   
   public RelationshipDTO()
   {
      
   }
   
   public RelationshipDTO(RelationshipDTO orig)
   {
      this.id = orig.id;
      this.typeId = orig.typeId;
      this.description = orig.description;
      this.descriptionMimeType = orig.descriptionMimeType;
      this.provenance = orig.provenance;
      this.relatedEntities = new HashSet<>(orig.relatedEntities);
      this.targetEntities = new HashSet<>(orig.targetEntities);
   }

   public static RelationshipDTO create(Relationship reln)
   {
      RelationshipDTO result = new RelationshipDTO();
      result.id = reln.getId();
      result.typeId = reln.getType().getIdentifier();
      result.description = reln.getDescription();
      result.descriptionMimeType = reln.getDescriptionFormat();

      // TODO provide better support for error messaging.
      result.provenance = ProvenanceDTO.create(reln.getProvenance());

      AnchorSet related = reln.getRelatedEntities();
      if (related != null)
      {
         for (Anchor anchor : related.getAnchors())
         {
            result.relatedEntities.add(AnchorDTO.create(anchor));
         }
      }

      AnchorSet target = reln.getTargetEntities();
      if (target != null)
      {
         for (Anchor anchor : target.getAnchors())
         {
            result.targetEntities.add(AnchorDTO.create(anchor));
         }
      }

      return result;
   }

   /**
    *
    * @param data
    * @param registry
    * @return
    * @throws RelationshipException If the supplied data cannot be parsed into a valid {@link Relationship}.
    */
   public static Relationship instantiate(RelationshipDTO data, RelationshipTypeRegistry registry) throws RelationshipException
   {
      String id = data.id;
      RelationshipType type = data.typeId == null ? null : registry.resolve(data.typeId);
      String desc = data.description;
      String descType = data.descriptionMimeType;
      Provenance prov = (data.provenance != null) ? ProvenanceDTO.instantiate(data.provenance) : new BasicProvenance();
      AnchorSet related = createAnchorSet(data.relatedEntities);
      AnchorSet target = createAnchorSet(data.targetEntities);

      return new BasicRelationship(id, type, desc, descType, prov, related, target);
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
}
