package edu.tamu.tcat.trc.entries.types.article.postgres;

import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;

public class BasicAuthor implements ArticleAuthor
{

   private final String id;
   private final String name;
   private final String affiliation;
   private final BasicContact info;
   
   public BasicAuthor()
   {
      this.id = null;
      this.name = null;
      this.affiliation = null;
      this.info = null;
   }
   
   public BasicAuthor(String id, String name, String affiliation, BasicContact info)
   {
      this.id = id;
      this.name = name;
      this.affiliation = affiliation;
      this.info = info;
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
   public ContactInfo getContactInfo()
   {
      return info;
   }
   
   public class BasicContact implements ContactInfo
   {
      private final String email;
      private final String phone;
      
      public BasicContact()
      {
         this.email = null;
         this.phone = null;
      }
      
      public BasicContact(String email, String phone)
      {
         this.email = email;
         this.phone = phone;
      }

      @Override
      public String getEmail()
      {
         return email;
      }

      @Override
      public String getPhone()
      {
         return phone;
      }
      
   }

}
