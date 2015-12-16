package edu.tamu.tcat.trc.entries.types.article.dto;

import java.util.Date;

import edu.tamu.tcat.trc.entries.types.article.ArticlePublication;

public class PublicationDTO
{
   public Date dateCreated;
   public Date dateModified;
   
   public static PublicationDTO create(ArticlePublication pub)
   {
      PublicationDTO dto = new PublicationDTO();
      dto.dateCreated = pub.getCreated();
      dto.dateModified = pub.getModified();
      return dto;
   }
   
   public static PublicationDTO copy(PublicationDTO orig)
   {
      PublicationDTO dto = new PublicationDTO();
      dto.dateCreated = orig.dateCreated;
      dto.dateModified = orig.dateModified;
      return dto;
   }
}
