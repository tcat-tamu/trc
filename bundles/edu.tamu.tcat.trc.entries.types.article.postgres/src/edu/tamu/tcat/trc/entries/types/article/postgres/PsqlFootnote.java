package edu.tamu.tcat.trc.entries.types.article.postgres;

import edu.tamu.tcat.trc.entries.types.article.Footnote;

public class PsqlFootnote implements Footnote
{

   private String id;
   private String text;

   public PsqlFootnote(String id, String text)
   {
      this.id = id;
      this.text = text;
   }

   @Override
   public String getId()
   {
      return this.id;
   }

   @Override
   public String getText()
   {
      return this.text;
   }

}
