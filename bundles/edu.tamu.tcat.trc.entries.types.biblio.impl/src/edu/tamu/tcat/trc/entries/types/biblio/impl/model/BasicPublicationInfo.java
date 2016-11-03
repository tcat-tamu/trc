package edu.tamu.tcat.trc.entries.types.biblio.impl.model;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.types.biblio.PublicationInfo;
import edu.tamu.tcat.trc.entries.types.biblio.impl.repo.DataModelV1.PublicationInfoDTO;

public class BasicPublicationInfo implements PublicationInfo
{
   private final String place;
   private final String publisher;
   private final DateDescription date;

   public BasicPublicationInfo(PublicationInfoDTO dto)
   {
      this.place = dto.place;
      this.publisher = dto.publisher;
      this.date = new DateDescriptionImpl(dto.date);
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