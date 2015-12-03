package edu.tamu.tcat.trc.entries.types.article.postgres;

import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;

public class BasicAuthor implements ArticleAuthor
{

   private final String id;
   private final String name;
   private final String affiliation;
   private final String email;
   
   public BasicAuthor()
   {
      this.id = null;
      this.name = null;
      this.affiliation = null;
      this.email = null;
   }
   
   public BasicAuthor(String id, String name, String affiliation, String email)
   {
      this.id = id;
      this.name = name;
      this.affiliation = affiliation;
      this.email = email;
   }
   
   @Override
   public String getId()
   {
      return this.id;
   }

   @Override
   public String getName()
   {
      return this.name;
   }

   @Override
   public String getAffiliation()
   {
      return this.affiliation;
   }

   @Override
   public String getEmail()
   {
      return this.email;
   }

}
