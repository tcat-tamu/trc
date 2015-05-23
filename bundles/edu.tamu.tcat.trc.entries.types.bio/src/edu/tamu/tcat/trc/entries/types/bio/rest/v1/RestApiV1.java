package edu.tamu.tcat.trc.entries.types.bio.rest.v1;

import java.util.Date;
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
   
   public static class SimplePersonResult
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
   }
}
