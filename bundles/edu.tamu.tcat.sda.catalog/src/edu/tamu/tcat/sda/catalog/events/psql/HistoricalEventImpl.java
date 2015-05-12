package edu.tamu.tcat.sda.catalog.events.psql;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import edu.tamu.tcat.catalogentries.events.DateDescription;
import edu.tamu.tcat.catalogentries.events.HistoricalEvent;
import edu.tamu.tcat.catalogentries.events.dv.DateDescriptionDV;
import edu.tamu.tcat.catalogentries.events.dv.HistoricalEventDV;

public class HistoricalEventImpl implements HistoricalEvent
{
   private final String id;
   private final String title;
   private final String description;
   private final String location;
   private final DateDescription eventDate;

   public HistoricalEventImpl(HistoricalEventDV src)
   {
      this.id = src.id;
      this.title = src.title;
      this.description = src.description;
      this.location = src.location;

      if (src.date == null) {
         // support legacy data
         if (src.eventDate != null) {
            Instant instant = Instant.ofEpochMilli(src.eventDate.getTime());
            LocalDate localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy");
            DateDescriptionDV dv = new DateDescriptionDV(localDate.format(formatter), localDate);

            this.eventDate = DateDescriptionDV.convert(dv);
         } else {
            this.eventDate = DateDescriptionDV.convert(new DateDescriptionDV("", null));
         }
      } else {
         this.eventDate = DateDescriptionDV.convert(src.date);
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
