package edu.tamu.tcat.trc.entries.types.article.dto;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.article.Theme;
import edu.tamu.tcat.trc.entries.types.article.Theme.Treatment;

public class ThemeDTO
{
   public String title;
   public String themeAbstract;
   public List<TreatmentDTO> treatments;

   public static ThemeDTO create(Theme theme)
   {
      ThemeDTO dto = new ThemeDTO();
      dto.title = theme.getTitle();
      dto.themeAbstract = theme.getAbstract();
      
      List<TreatmentDTO> treatmentDTO = new ArrayList<>();
      theme.getTreatments().forEach((t) -> {
         treatmentDTO.add(TreatmentDTO.create(t));
      });
      dto.treatments = treatmentDTO;
      return dto;
   }
   
   public static class TreatmentDTO
   {
      public String type;
      public String uri;
      
      public static TreatmentDTO create(Treatment t)
      {
         TreatmentDTO dto = new TreatmentDTO();
         dto.type = t.getType();
         dto.uri = t.getURI();
         return dto;
      }
   }
}
