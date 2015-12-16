package edu.tamu.tcat.trc.entries.types.article.dto;

import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor.ContactInfo;

public class ArticleAuthorDTO
{
   public String id;
   public String name;
   public String affiliation;
   public ContactInfoDTO contact;
   
   public static ArticleAuthorDTO create(ArticleAuthor author)
   {
      ArticleAuthorDTO a = new ArticleAuthorDTO();
      a.id = author.getId();
      a.name = author.getName();
      a.affiliation = author.getAffiliation();
      a.contact = ContactInfoDTO.create(author.getContactInfo());
      return a;
   }
   
   public static ArticleAuthorDTO copy(ArticleAuthorDTO orig)
   {
      ArticleAuthorDTO dto = new ArticleAuthorDTO();
      dto.id = orig.id;
      dto.name = orig.name;
      dto.affiliation = orig.affiliation;
      dto.contact = orig.contact;
      return dto;
   }
   
   public static class ContactInfoDTO
   {
      public String email;
      public String phone;
      
      public static ContactInfoDTO create(ContactInfo contactInfo)
      {
         ContactInfoDTO dto = new ContactInfoDTO();
         dto.email = contactInfo.getEmail();
         dto.phone = contactInfo.getPhone();
         return dto;
      }
      
      public static ContactInfoDTO create(String email, String phone)
      {
         ContactInfoDTO dto = new ContactInfoDTO();
         dto.email = email;
         dto.phone = phone;
         return dto;
      }
   }
}
