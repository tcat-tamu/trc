package edu.tamu.tcat.trc.entries.types.biblio.impl.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.types.biblio.impl.repo.DataModelV1;
import edu.tamu.tcat.trc.entries.types.biblio.impl.repo.DataModelV1.DateDescriptionDTO;

final class DateDescriptionImpl implements DateDescription
{
   private final static Logger logger = Logger.getLogger(DateDescriptionImpl.class.getName());

   private final String description;
   private final LocalDate value;

   DateDescriptionImpl(DataModelV1.DateDescriptionDTO dv)
   {
      this.description = dv.description;
      this.value = extractCalendarDate(dv);
   }

   private static LocalDate extractCalendarDate(DataModelV1.DateDescriptionDTO dv)
   {
      try
      {
         return (dv.calendar != null && !dv.calendar.trim().isEmpty())
                  ? LocalDate.parse(dv.calendar, DateTimeFormatter.ISO_LOCAL_DATE) : null;
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