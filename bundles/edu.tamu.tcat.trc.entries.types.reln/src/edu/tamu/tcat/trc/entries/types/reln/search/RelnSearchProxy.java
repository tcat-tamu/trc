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
package edu.tamu.tcat.trc.entries.types.reln.search;

import java.util.HashSet;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.dto.AnchorDTO;
import edu.tamu.tcat.trc.entries.types.reln.dto.ProvenanceDTO;

/**
 * JSON serializable summary information about a relationship entry.
 * Intended to be returned when only a brief summary is required to save
 * data transfer and parsing resources.
 */
public class RelnSearchProxy
{
   public String id;
   public String typeId;
   public String description;
   public String descriptionMimeType;
   public ProvenanceDTO provenance;
   public Set<AnchorDTO> relatedEntities;
   public Set<AnchorDTO> targetEntities;

   public static RelnSearchProxy create(Relationship reln)
   {
      RelnSearchProxy result = new RelnSearchProxy();
      result.id = reln.getId();
      result.typeId = reln.getType().getIdentifier();
      result.description = reln.getDescription();
      result.descriptionMimeType = reln.getDescriptionFormat();

      result.provenance = ProvenanceDTO.create(reln.getProvenance());

      AnchorSet related = reln.getRelatedEntities();
      if (related != null)
      {
         result.relatedEntities = new HashSet<>();
         for (Anchor anchor : related.getAnchors())
         {
            result.relatedEntities.add(AnchorDTO.create(anchor));
         }
      }

      AnchorSet target = reln.getTargetEntities();
      if (target != null)
      {
         result.targetEntities = new HashSet<>();
         for (Anchor anchor : target.getAnchors())
         {
            result.targetEntities.add(AnchorDTO.create(anchor));
         }
      }

      return result;
   }
}
