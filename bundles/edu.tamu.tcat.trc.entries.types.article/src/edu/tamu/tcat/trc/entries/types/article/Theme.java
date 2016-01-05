package edu.tamu.tcat.trc.entries.types.article;

import java.util.List;

public interface Theme
{
   String getTitle();
   String getAbstract();
   List<ArticleRefs> getArticleRefs();
   
   public interface ArticleRefs
   {
      String getType();
      String getURI();
      String getId();
   }
}
