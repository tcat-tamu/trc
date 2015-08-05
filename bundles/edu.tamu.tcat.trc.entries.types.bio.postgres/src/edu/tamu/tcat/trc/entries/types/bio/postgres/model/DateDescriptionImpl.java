package edu.tamu.tcat.trc.entries.types.bio.postgres.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.common.dto.DateDescriptionDTO;

public class DateDescriptionImpl implements DateDescription
{
   private static final Logger logger = Logger.getLogger(DateDescriptionDTO.class.getName());

   public static java.time.format.DateTimeFormatter Iso8601Formatter = DateTimeFormatter.ISO_LOCAL_DATE;

   private final String description;
   private final LocalDate value;

   public DateDescriptionImpl(DateDescriptionDTO dv)
   {
      if (dv == null)
         dv = new DateDescriptionDTO();

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
