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
package edu.tamu.tcat.trc.entries.types.bio.impl.model;

import static java.text.MessageFormat.format;

import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.common.HistoricalEvent;
import edu.tamu.tcat.trc.entries.types.bio.impl.repo.DataModelV1;

public class HistoricalEventImpl implements HistoricalEvent
{
   private final static Logger logger = Logger.getLogger(HistoricalEventImpl.class.getName());

   private final String id;
   private final String title;
   private final String description;
   private final String location;
   private final DateDescription eventDate;

   public HistoricalEventImpl(DataModelV1.HistoricalEvent src)
   {
      this.id = src.id;
      this.title = src.title;
      this.description = src.description;
      this.location = src.location;

      if (src.date == null) {
         this.eventDate = new DateDescriptionImpl(null, null);
      } else {
         String desc = src.date.description;
         LocalDate calendar = null;
         try {
            calendar = (src.date.calendar != null) ? LocalDate.parse(src.date.calendar) : null;
         } catch (Exception ex) {
            String msg = "Bad calendar date stored for historical event {0} [{1}]: {2}";
            logger.log(Level.WARNING, format(msg, this.title, this.id, src.date.calendar));
         }

         this.eventDate = new DateDescriptionImpl(desc, calendar);
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
