package edu.tamu.tcat.trc.entries.types.article.dto;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.article.Citation;

public class CitationDTO
{
   public String id;
   public List<CitationItemDTO> citationItems;
   
   public static CitationDTO create(Citation cit)
   {
      List<CitationItemDTO> cItems = new ArrayList<>();
      CitationDTO dto = new CitationDTO();
      dto.id = cit.getId();
      cit.getItems().forEach((c) -> {
         cItems.add(CitationItemDTO.create(c));
      });
      
      dto.citationItems = new ArrayList<>(cItems);
      return dto;
   }
}
