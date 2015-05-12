package edu.tamu.tcat.trc.entries.bib.dto;

import edu.tamu.tcat.catalogentries.events.DateDescription;
import edu.tamu.tcat.catalogentries.events.dv.DateDescriptionDV;
import edu.tamu.tcat.trc.entries.bib.PublicationInfo;

public class PublicationInfoDV
{
   public String publisher;
   public String place;
   public DateDescriptionDV date;

   public static PublicationInfoDV create(PublicationInfo pubInfo)
   {
      PublicationInfoDV dto = new PublicationInfoDV();
      dto.publisher = pubInfo.getPublisher();
      dto.place = pubInfo.getLocation();
      dto.date = new DateDescriptionDV(pubInfo.getPublicationDate());

      return dto;
   }

   public static PublicationInfo instantiate(PublicationInfoDV pubInfo)
   {
      return new PublicationImpl(pubInfo.place, pubInfo.publisher, DateDescriptionDV.convert(pubInfo.date));
   }

   public static class PublicationImpl implements PublicationInfo
   {
      private final String place;
      private final String publisher;
      private final DateDescription date;

      public PublicationImpl(String place, String publisher, DateDescription date)
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
}
