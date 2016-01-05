package edu.tamu.tcat.trc.entries.types.article.dto;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.article.Theme;
import edu.tamu.tcat.trc.entries.types.article.Theme.ArticleRefs;;

public class ThemeDTO
{
   public String title;
   public String themeAbstract;
   public List<ArticleRefDTO> articleRefsDTO;

   public static ThemeDTO create(Theme theme)
   {
      ThemeDTO dto = new ThemeDTO();
      dto.title = theme.getTitle();
      dto.themeAbstract = theme.getAbstract();
      
      List<ArticleRefDTO> articleRefsDTO = new ArrayList<>();
      theme.getArticleRefs().forEach((t) -> {
         articleRefsDTO.add(ArticleRefDTO.create(t));
      });
      dto.articleRefsDTO = articleRefsDTO;
      return dto;
   }
   
   public static class ArticleRefDTO
   {
      public String id;
      public String type;
      public String uri;
      
      public static ArticleRefDTO create(ArticleRefs ar)
      {
         ArticleRefDTO dto = new ArticleRefDTO();
         dto.id = ar.getId();
         dto.type = ar.getType();
         dto.uri = ar.getURI();
         return dto;
      }
   }
}
