package edu.tamu.tcat.trc.entries.types.article.postgres;

import edu.tamu.tcat.trc.entries.types.article.ArticleLink;

public class PsqlArticleLink implements ArticleLink
{
   

   private String id;
   private String title;
   private String type;
   private String uri;
   private String rel;

   public PsqlArticleLink(String id, String title, String type, String uri, String rel)
   {
      this.id = id;
      this.title = title;
      this.type = type;
      this.uri = uri;
      this.rel = rel;
   }

   @Override
   public String getId()
   {
      return this.id;
   }

   @Override
   public String getTitle()
   {
      return this.title;
   }

   @Override
   public String getType()
   {
      return this.type;
   }

   @Override
   public String getUri()
   {
      return this.uri;
   }

   @Override
   public String getRel()
   {
      return this.rel;
   }

}
