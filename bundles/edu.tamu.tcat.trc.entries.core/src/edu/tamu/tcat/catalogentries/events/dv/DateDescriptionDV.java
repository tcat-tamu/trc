package edu.tamu.tcat.catalogentries.events.dv;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import edu.tamu.tcat.catalogentries.events.DateDescription;

/**
 * A simple representation of historical date information that includes both a calendar
 * data (a Java {@link Instant}) and a description of that date.
 */
public class DateDescriptionDV
{
   private static final Logger logger = Logger.getLogger(DateDescriptionDV.class.getName());

   public static java.time.format.DateTimeFormatter Iso8601Formatter = DateTimeFormatter.ISO_LOCAL_DATE;

   /** ISO 8601 local (YYYY-MM-DD) representation of this date. */
   public String calendar;

   /** A human readable description of this date. */
   public String description;     // NOTE use this to capture intended degree of precision

   public DateDescriptionDV()
   {
   }

   public DateDescriptionDV(String description, LocalDate calendar)
   {
      this.description = description;
      this.calendar = (calendar == null) ? null : Iso8601Formatter.format(calendar);
   }

   public DateDescriptionDV(DateDescription date)
   {
      LocalDate d = date.getCalendar();
      if (d != null)
      {
         this.calendar = Iso8601Formatter.format(d);
      }

      // TODO convert legacy eventDate into DateDescriptionDV and set to null
      this.description = date.getDescription();
   }

   public static DateDescription convert(DateDescriptionDV dv)
   {
      return dv == null ? new DateDescriptionImpl(new DateDescriptionDV()) : new DateDescriptionImpl(dv);
   }

   private static final class DateDescriptionImpl implements DateDescription
   {
      private final String description;
      private final LocalDate value;

      DateDescriptionImpl(DateDescriptionDV dv)
      {
         this.description = dv.description;
         this.value = extractCalendarDate(dv);
      }

      private static LocalDate extractCalendarDate(DateDescriptionDV dv)
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
