package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.types.biblio.PublicationInfo;

public class BasicPublicationInfo implements PublicationInfo
{
   private final String place;
   private final String publisher;
   private final DateDescription date;

   public BasicPublicationInfo(String place, String publisher, DateDescription date)
   {
      this.place = place;
      this.publisher = publisher;
      this.date = date;
   }

   @Override
   public String getLocation()
   {
      return place;
   }

   @Override
   public String getPublisher()
   {
      return publisher;
   }

   @Override
   public DateDescription getPublicationDate()
   {
      return date;
   }

}