package edu.tamu.tcat.trc.entries.types.article.postgres;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.article.Theme;
import edu.tamu.tcat.trc.entries.types.article.dto.ThemeDTO.ArticleRefDTO;

public class PsqlTheme implements Theme
{
   private String title;
   private String themeAbs;
   private List<ArticleRefs> articleRefs;

   public PsqlTheme(String title, String themeAbs, List<ArticleRefDTO> articleRefs)
   {
      this.title = title;
      this.themeAbs = themeAbs;
      if (articleRefs == null)
         this.articleRefs = new ArrayList<>();
      else
         this.articleRefs = new ArrayList<>(getTreatments(articleRefs));
      
   }
   
   private List<ArticleRefs> getTreatments(List<ArticleRefDTO> articleRefsDTO)
   {
      List<ArticleRefs> ar = new ArrayList<>();
      articleRefsDTO.forEach((articleDTO)->
      {
         ar.add(new PsqlTreatment(articleDTO.id, articleDTO.type, articleDTO.uri));
      });
      return ar;
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
   public List<ArticleRefs> getArticleRefs()
   {
      return articleRefs;
   }
   
   public class PsqlTreatment implements ArticleRefs
   {
      private String id;
      private String type;
      private String uri;

      public PsqlTreatment(String id, String type, String uri)
      {
         this.id = id;
         this.type = type;
         this.uri = uri;
      }
      
      @Override
      public String getId()
      {
         return id;
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
