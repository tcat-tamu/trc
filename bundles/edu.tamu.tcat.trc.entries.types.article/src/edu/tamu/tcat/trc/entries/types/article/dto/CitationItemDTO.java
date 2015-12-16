package edu.tamu.tcat.trc.entries.types.article.dto;

import edu.tamu.tcat.trc.entries.types.article.Citation.CitationItem;

public class CitationItemDTO
{
   public String id;
   public String locator;
   public String label;
   
   public static CitationItemDTO create(CitationItem item)
   {
      CitationItemDTO dto = new CitationItemDTO();
      dto.id = item.getId();
      dto.locator = item.getLocator();
      dto.label = item.getLabel();
      return dto;
   }
}
