package edu.tamu.tcat.trc.entries.types.article.impl.repo;

import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor.ContactInfo;

public abstract class ModelAdapter
{
   public DataModelV1.Article adapt(Article author)
   {
      DataModelV1.Article dto = new DataModelV1.Article();
      dto.id = author.getId();
      dto.title = author.getTitle();
      dto.slug = author.getSlug();
      dto.contentType = author.getContentType();
      dto.articleType = author.getArticleType();
      dto.articleAbstract = author.getAbstract();
      dto.body = author.getBody();

      author.getAuthors().stream().map(ModelAdapter::adapt);

      return dto;
   }

   private static DataModelV1.ArticleAuthor adapt(ArticleAuthor author)
   {
      DataModelV1.ArticleAuthor dto = new DataModelV1.ArticleAuthor();
      dto.id = author.getId();
      dto.name = author.getName();
      dto.first = author.getFirstname();
      dto.last = author.getLastname();
      dto.affiliation = author.getAffiliation();

      dto.contact = adapt(author.getContactInfo());

      return dto;
   }

   private static DataModelV1.ContactInfo adapt(ContactInfo contact)
   {
      DataModelV1.ContactInfo dto = new DataModelV1.ContactInfo();
      dto.email = contact.getEmail();
      dto.phone = contact.getPhone();

      return dto;
   }
}
