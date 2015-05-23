package edu.tamu.tcat.trc.entries.common.dto;

import java.util.Date;

import edu.tamu.tcat.trc.entries.common.HistoricalEvent;

public class HistoricalEventDTO
{

   public String id;
   public String title;
   public String description;
   public String location;

   /** The date this event took place. */
   public DateDescriptionDTO date;

   /** Replaced by date. */
   @Deprecated
   public Date eventDate;

   public HistoricalEventDTO()
   {

   }

   public HistoricalEventDTO(HistoricalEvent orig)
   {
      this.id = orig.getId();
      this.title = orig.getTitle();
      this.description = orig.getDescription();
      this.location = orig.getLocation();
      this.date = new DateDescriptionDTO(orig.getDate());
   }
}
