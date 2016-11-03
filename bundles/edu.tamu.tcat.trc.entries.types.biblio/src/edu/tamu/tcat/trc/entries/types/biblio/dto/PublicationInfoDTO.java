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
package edu.tamu.tcat.trc.entries.types.biblio.dto;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.types.biblio.PublicationInfo;

@Deprecated
public class PublicationInfoDTO
{
   public String place;
   public String publisher;
   // TODO: should default date description be null or empty object?
   public DateDescriptionDTO date;

   public PublicationInfoDTO()
   {

   }

   public PublicationInfoDTO(PublicationInfoDTO orig)
   {
      if (orig != null)
      {
         this.place = orig.place;
         this.publisher = orig.publisher;
         this.date = new DateDescriptionDTO(orig.date);
      }
   }

   public static PublicationInfoDTO create(PublicationInfo pubInfo)
   {
      PublicationInfoDTO dto = new PublicationInfoDTO();

      if (pubInfo == null)
      {
         return dto;
      }

      dto.place = pubInfo.getLocation();

      dto.publisher = pubInfo.getPublisher();

      DateDescription date = pubInfo.getPublicationDate();
      if (date != null)
      {
         dto.date = DateDescriptionDTO.create(date);
      }

      return dto;
   }
}
