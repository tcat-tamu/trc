package edu.tamu.tcat.trc.entries.types.bio.postgres;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataModelV1
{

   public static java.time.format.DateTimeFormatter Iso8601Formatter = DateTimeFormatter.ISO_LOCAL_DATE;
   
   public static class Person
   {
      public String id;
      public PersonName displayName;
      public List<PersonName> names;
      public HistoricalEvent birth;
      public HistoricalEvent death;
      public String summary;
      
      
      public static Person copy(Person orig)
      {
         Person dto = new Person();
         dto.id = orig.id;
         dto.displayName = orig.displayName;
         dto.names = new ArrayList<>(orig.names);
         dto.birth = orig.birth;
         dto.death = orig.death;
         dto.summary = orig.summary;
         return dto;
      }
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
      public Date eventDate;
   }
   
   public static class DateDescription
   {
      /** ISO 8601 local (YYYY-MM-DD) representation of this date. */
      public String calendar;

      /** A human readable description of this date. */
      public String description;     // NOTE use this to capture intended degree of precision

      public static DateDescription create(String description, LocalDate calendar)
      {
         DateDescription dto = new DateDescription();

         dto.description = description;

         dto.calendar = (calendar == null) ? null : Iso8601Formatter.format(calendar);

         return dto;
      }
   }

}
