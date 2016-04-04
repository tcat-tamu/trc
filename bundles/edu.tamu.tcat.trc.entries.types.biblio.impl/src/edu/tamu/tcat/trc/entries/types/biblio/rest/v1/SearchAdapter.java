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
package edu.tamu.tcat.trc.entries.types.biblio.rest.v1;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.search.BiblioSearchProxy;

/**
 * An encapsulation of adapter methods to convert between the search API and
 * the {@link RestApiV1} schema DTOs.
 */
public class SearchAdapter
{
   public static List<RestApiV1.WorkSearchResult> toDTO(List<BiblioSearchProxy> origList)
   {
      if (origList == null)
         return null;

      List<RestApiV1.WorkSearchResult> dtoList = new ArrayList<>();
      for (BiblioSearchProxy orig : origList)
      {
         RestApiV1.WorkSearchResult dto = new RestApiV1.WorkSearchResult();
         dto.id = orig.id;
         dto.label = orig.label;
         dto.title = orig.title;
         dto.uri = orig.uri;
         dto.pubYear = orig.pubYear;
         dto.summary = orig.summary;
         if (orig.authors != null)
         {
            dto.authors = new ArrayList<>();
            for (AuthorReferenceDTO auth : orig.authors)
               dto.authors.add(RepoAdapter.toDTO(auth));
         }

         dtoList.add(dto);
      }

      return dtoList;
   }
}
