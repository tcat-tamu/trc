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

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import edu.tamu.tcat.trc.entries.common.DateDescription;

/**
 * A simple representation of historical date information that includes both a calendar
 * data (a Java {@link Instant}) and a description of that date.
 */
public class DateDescriptionDTO
{
   private static final Logger logger = Logger.getLogger(DateDescriptionDTO.class.getName());

   public static java.time.format.DateTimeFormatter Iso8601Formatter = DateTimeFormatter.ISO_LOCAL_DATE;

   /** ISO 8601 local (YYYY-MM-DD) representation of this date. */
   public String calendar;

   /** A human readable description of this date. */
   public String description;     // NOTE use this to capture intended degree of precision

   public DateDescriptionDTO()
   {
   }

   public DateDescriptionDTO(String description, LocalDate calendar)
   {
      this.description = description;
      this.calendar = (calendar == null) ? null : Iso8601Formatter.format(calendar);
   }

   public DateDescriptionDTO(DateDescription date)
   {
      LocalDate d = date.getCalendar();
      if (d != null)
      {
         this.calendar = Iso8601Formatter.format(d);
      }

      // TODO convert legacy eventDate into DateDescriptionDTO and set to null
      this.description = date.getDescription();
   }

   public static DateDescription convert(DateDescriptionDTO dv)
   {
      return dv == null ? new DateDescriptionImpl(new DateDescriptionDTO()) : new DateDescriptionImpl(dv);
   }

   private static final class DateDescriptionImpl implements DateDescription
   {
      private final String description;
      private final LocalDate value;

      DateDescriptionImpl(DateDescriptionDTO dv)
      {
         this.description = dv.description;
         this.value = extractCalendarDate(dv);
      }

      private static LocalDate extractCalendarDate(DateDescriptionDTO dv)
      {
         try
         {
            return (dv.calendar != null && !dv.calendar.trim().isEmpty())
                     ? LocalDate.parse(dv.calendar, Iso8601Formatter) : null;
         }
         catch (Exception ex)
         {
            logger.info("Invalid date supplied [" + dv.calendar + "]. Converting to null.");
            return null;
         }
      }

      @Override
      public String getDescription()
      {
         return description;
      }

      @Override
      public LocalDate getCalendar()
      {
         return value;
      }
   }
}
