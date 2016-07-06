package edu.tamu.tcat.trc.entries.types.article.docrepo;

import edu.tamu.tcat.trc.entries.types.article.docrepo.DataModelV1.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.repo.AuthorMutator;
import edu.tamu.tcat.trc.repo.ChangeSet;

public class ArticleAuthorMutatorImpl implements AuthorMutator
{

   private final String id;
   private final ChangeSet<ArticleAuthor> changes;

   public ArticleAuthorMutatorImpl(String id, ChangeSet<ArticleAuthor> partial)
   {
      this.id = id;
      this.changes = partial;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public void setFirstname(String name)
   {
      changes.add("firstname", author -> author.first = name);
   }

   @Override
   public void setLastname(String name)
   {
      changes.add("lastname", author -> author.last = name);
   }

   @Override
   public void setAffiliation(String affiliation)
   {
      changes.add("affiliation", author -> author.affiliation = affiliation);
   }

   @Override
   public void setEmailAddress(String email)
   {
      changes.add("email", author -> {
         if (author.contact == null)
            author.contact = new DataModelV1.ContactInfo();

         author.contact.email = email;
      });
   }
}
