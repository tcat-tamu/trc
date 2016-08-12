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
package edu.tamu.tcat.trc.entries.types.bio.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.common.HistoricalEvent;

public class HistoricalEventImpl implements HistoricalEvent
{
   private final String id;
   private final String title;
   private final String description;
   private final String location;
   private final DateDescription eventDate;

   public HistoricalEventImpl(HistoricalEventDTO src)
   {
      this.id = src.id;
      this.title = src.title;
      this.description = src.description;
      this.location = src.location;

      if (src.date == null) {
         // support legacy data
         if (src.eventDate != null) {
            Instant instant = Instant.ofEpochMilli(src.eventDate.getTime());
            LocalDate localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy");
            DateDescriptionDTO dv = DateDescriptionDTO.create(localDate.format(formatter), localDate);

            this.eventDate = DateDescriptionDTO.convert(dv);
         } else {
            this.eventDate = DateDescriptionDTO.convert(DateDescriptionDTO.create("", null));
         }
      } else {
         this.eventDate = DateDescriptionDTO.convert(src.date);
      }
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String getTitle()
   {
      return title;
   }

   @Override
   public String getDescription()
   {
      return description;
   }

   @Override
   public String getLocation()
   {
      return location;
   }

   @Override
   public DateDescription getDate()
   {
      return eventDate;
   }


}
