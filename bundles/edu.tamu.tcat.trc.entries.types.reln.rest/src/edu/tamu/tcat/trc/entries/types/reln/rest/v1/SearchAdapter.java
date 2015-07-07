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
package edu.tamu.tcat.trc.entries.types.reln.rest.v1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.reln.dto.AnchorDTO;
import edu.tamu.tcat.trc.entries.types.reln.search.RelnSearchProxy;

/**
 * An encapsulation of adapter methods to convert between the search API and
 * the {@link RestApiV1} schema DTOs.
 */
public class SearchAdapter
{
   public static List<RestApiV1.RelationshipSearchResult> toDTO(List<RelnSearchProxy> origList)
   {
      if (origList == null)
         return null;
      List<RestApiV1.RelationshipSearchResult> dtoList = new ArrayList<>();
      for (RelnSearchProxy orig : origList)
      {
         RestApiV1.RelationshipSearchResult dto = new RestApiV1.RelationshipSearchResult();
         dto.id = orig.id;
         dto.description = orig.description;
         dto.descriptionMimeType = orig.descriptionMimeType;
         dto.typeId = orig.typeId;
         dto.provenance = RepoAdapter.toDTO(orig.provenance);
         if (orig.relatedEntities != null)
         {
            dto.relatedEntities = new HashSet<>();
            for (AnchorDTO dv : orig.relatedEntities)
            {
               dto.relatedEntities.add(RepoAdapter.toDTO(dv));
            }
         }
         if (orig.targetEntities != null)
         {
            dto.targetEntities = new HashSet<>();
            for (AnchorDTO dv : orig.targetEntities)
            {
               dto.targetEntities.add(RepoAdapter.toDTO(dv));
            }
         }

         dtoList.add(dto);
      }

      return dtoList;
   }
}
