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
package edu.tamu.tcat.trc.entries.common.dto;

import java.util.Date;

import edu.tamu.tcat.trc.entries.common.HistoricalEvent;

public class HistoricalEventDTO
{

   public String id;
   public String title;
   public String description;
   public String location;

   /** The date this event took place. */
   public DateDescriptionDTO date;

   /** Replaced by date. */
   @Deprecated
   public Date eventDate;

   public HistoricalEventDTO()
   {

   }

   public HistoricalEventDTO(HistoricalEvent orig)
   {
      this.id = orig.getId();
      this.title = orig.getTitle();
      this.description = orig.getDescription();
      this.location = orig.getLocation();
      this.date = new DateDescriptionDTO(orig.getDate());
   }
}
