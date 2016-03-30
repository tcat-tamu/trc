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
package edu.tamu.tcat.trc.entries.types.bio.rest.v1.internal;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.bio.dto.PersonNameDTO;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.RestApiV1;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.RestApiV1.PersonName;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.RestApiV1.SimplePerson;
import edu.tamu.tcat.trc.entries.types.bio.search.BioSearchProxy;

/**
 * An encapsulation of adapter methods to convert between the search API and
 * the {@link RestApiV1} schema DTOs.
 */
public class SearchAdapter
{
   public static List<RestApiV1.SimplePerson> toDTO(List<BioSearchProxy> origList)
   {
      if (origList == null)
         return null;
      List<RestApiV1.SimplePerson> dtoList = new ArrayList<>();
      for (BioSearchProxy orig : origList)
      {
         RestApiV1.SimplePerson dto = new RestApiV1.SimplePerson();
         dto.id = orig.id;
         dto.name = toDTO(orig.displayName);
         dto.label = orig.formattedName;
         dto.summaryExcept = orig.summaryExcerpt;

         dtoList.add(dto);
      }

      return dtoList;
   }

   private static RestApiV1.PersonName toDTO(PersonNameDTO orig)
   {
      if (orig == null)
         return null;
      RestApiV1.PersonName dto = new RestApiV1.PersonName();
      dto.title = orig.title;
      dto.givenName = orig.givenName;
      dto.middleName = orig.middleName;
      dto.familyName = orig.familyName;
      dto.suffix = orig.suffix;

      dto.label = orig.displayName;
      return dto;
   }
}