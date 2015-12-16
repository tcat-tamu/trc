package edu.tamu.tcat.trc.entries.types.article.postgres;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.article.Theme;
import edu.tamu.tcat.trc.entries.types.article.dto.ThemeDTO.TreatmentDTO;

public class PsqlTheme implements Theme
{
   private String title;
   private String themeAbs;
   private List<Treatment> treatment;

   public PsqlTheme(String title, String themeAbs, List<TreatmentDTO> treatments)
   {
      this.title = title;
      this.themeAbs = themeAbs;
      if (treatments == null)
         this.treatment = new ArrayList<>();
      else
         this.treatment = new ArrayList<>(getTreatments(treatments));
      
   }
   
   private List<Treatment> getTreatments(List<TreatmentDTO> treatments)
   {
      List<Treatment> t = new ArrayList<>();
      treatments.forEach((treat)->
      {
         t.add(new PsqlTreatment(treat.type, treat.uri));
      });
      return t;
   }

   @Override
   public String getTitle()
   {
      return title;
   }

   @Override
   public String getAbstract()
   {
      return themeAbs;
   }

   @Override
   public List<Treatment> getTreatments()
   {
      return treatment;
   }
   
   public class PsqlTreatment implements Treatment
   {
      
      private String type;
      private String uri;

      public PsqlTreatment(String type, String uri)
      {
         this.type = type;
         this.uri = uri;
      }

      @Override
      public String getType()
      {
         return type;
      }

      @Override
      public String getURI()
      {
         return uri;
      }
   }
}
