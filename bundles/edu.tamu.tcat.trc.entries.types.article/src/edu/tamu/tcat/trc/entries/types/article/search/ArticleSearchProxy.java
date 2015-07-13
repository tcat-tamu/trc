package edu.tamu.tcat.trc.entries.types.article.search;

import edu.tamu.tcat.trc.entries.types.article.Article;

public class ArticleSearchProxy
{

   public String id;
   public String title;
   public String authorId;
   public String associatedEntity;
   public String content;
   public String mimeType;


   public ArticleSearchProxy()
   {
   }

   public ArticleSearchProxy(Article article)
   {
      this.id = article.getId().toString();
      this.title = article.getTitle();
      this.authorId = article.getAuthorId().toString();
      this.associatedEntity = article.getEntity().toString();
      this.content = article.getContent();
      this.mimeType = article.getMimeType();
   }
}
