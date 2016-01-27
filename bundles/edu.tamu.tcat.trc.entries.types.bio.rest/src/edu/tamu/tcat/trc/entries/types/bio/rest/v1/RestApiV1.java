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
package edu.tamu.tcat.trc.entries.types.bio.rest.v1;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * An encapsulation of the data vehicle types used to process JSON requests and responses
 * for version 1 of the TRC REST API for Persons.
 */
public class RestApiV1
{
   public static class PersonId
   {
      public String id;
   }

   /**
    * Represents a Person
    */
   public static class Person
   {
      public String id;
      public PersonName displayName;
      public Set<PersonName> names;
      public HistoricalEvent birth;
      public HistoricalEvent death;
      public String summary;
   }

   public static class PersonName
   {
      public String title;
      public String givenName;
      public String middleName;
      public String familyName;
      public String suffix;

      public String displayName;
   }

   public static class HistoricalEvent
   {

      public String id;
      public String title;
      public String description;
      public String location;

      /** The date this event took place. */
      public DateDescription date;

      /** Replaced by date. */
      @Deprecated
      public Date eventDate;
   }

   public static class DateDescription
   {
      /** ISO 8601 local (YYYY-MM-DD) representation of this date. */
      public String calendar;

      /** A human readable description of this date. */
      public String description;
   }

   public static class PersonSearchResultSet
   {
      public List<PersonSearchResult> items;
      /** The querystring that resulted in this result set */
      public String qs;
      public String qsNext;
      public String qsPrev;
   }

   public static class PersonSearchResult
   {
      /**
       * ID corresponding to the {@link Person} object that this simple data vehicle represents.
       */
      public String id;

      /**
       * Display name for this person (for use when populating fields)
       */
      public PersonName displayName;

      /**
       * Formatted name to display e.g. when linking to the underlying {@link Person}.
       *
       * This is essentially a string representation of the display name plus the lifespan of the person.
       */
      public String formattedName;

      /**
       * A leading excerpt from the full bibliographic summary
       */
      public String summary;
   }
}
