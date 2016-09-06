package edu.tamu.tcat.trc.entries.types.bio.postgres.model;

import java.time.LocalDate;

import edu.tamu.tcat.trc.entries.common.DateDescription;

public class DateDescriptionImpl implements DateDescription
{
   private final String description;
   private final LocalDate value;

   public DateDescriptionImpl(String description, LocalDate calendar)
   {
      this.description = description;
      this.value = calendar;
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
