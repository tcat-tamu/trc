package edu.tamu.tcat.trc.entries.types.article.dto;

import edu.tamu.tcat.trc.entries.types.article.Citation;

public class CitationDTO
{
   public String id;
   public CitationItemDTO citationItems;
   public CitationPropertiesDTO properties;
   public String suppressAuthor;
   
   public static CitationDTO create(Citation cit)
   {
      CitationDTO dto = new CitationDTO();
      dto.id = cit.getId();
      dto.properties = CitationPropertiesDTO.create();
      dto.suppressAuthor = cit.getSupressAuthor();
      
      dto.citationItems = CitationItemDTO.create(cit.getItems());
      
      return dto;
   }
   
   public static class CitationPropertiesDTO
   {
      public static CitationPropertiesDTO create()
      {
         return new CitationPropertiesDTO();
      }
   }
   
}
