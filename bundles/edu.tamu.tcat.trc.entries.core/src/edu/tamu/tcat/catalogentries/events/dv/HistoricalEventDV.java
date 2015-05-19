package edu.tamu.tcat.catalogentries.events.dv;

import java.util.Date;

import edu.tamu.tcat.catalogentries.events.HistoricalEvent;

public class HistoricalEventDV
{

   public String id;
   public String title;
   public String description;
   public String location;

   /** The date this event took place. */
   public DateDescriptionDV date;

   /** Replaced by date. */
   @Deprecated
   public Date eventDate;

   public HistoricalEventDV()
   {

   }

   public HistoricalEventDV(HistoricalEvent orig)
   {
      this.id = orig.getId();
      this.title = orig.getTitle();
      this.description = orig.getDescription();
      this.location = orig.getLocation();
      this.date = new DateDescriptionDV(orig.getDate());
   }
}
