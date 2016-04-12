package edu.tamu.tcat.trc.entries.types.article.postgres;

import java.util.Date;

import edu.tamu.tcat.trc.entries.types.article.ArticlePublication;

public class BasicPublication implements ArticlePublication
{
   private final Date created;
   private final Date modified;
   
   public BasicPublication()
   {
      this.created = null;
      this.modified = null;
   }
   
   public BasicPublication(Date created, Date modified)
   {
      this.created = created;
      this.modified = modified;
   }

   @Override
   public Date getCreated()
   {
      return created;
   }

   @Override
   public Date getModified()
   {
      return modified;
   }

}
