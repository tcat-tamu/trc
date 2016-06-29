/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.entries.types.article.search;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor.ContactInfo;
import edu.tamu.tcat.trc.entries.types.article.ArticlePublication;

public class ArticleSearchProxy
{
   // Returns a light weight article for search results
   public String id;
   public String title;
   public String articleType;
   public List<AuthorRef> authors;
   public PublicationRef info;

   public ArticleSearchProxy()
   {
   }

   public ArticleSearchProxy(Article article)
   {
      this.id = article.getId().toString();
      this.title = article.getTitle();
      this.articleType = article.getArticleType();

      this.authors = article.getAuthors().stream()
            .map(AuthorRef::new)
            .collect(Collectors.toList());

      this.info = new PublicationRef(article.getPublicationInfo());
   }

   public static class AuthorRef
   {
      public String id;
      public String name;
      public String affiliation;
      public ContactInfoRef contactinfo;

      public AuthorRef(){}

      public AuthorRef(ArticleAuthor auth)
      {
         this.id = auth.getId();
         this.name = auth.getName();
         this.affiliation = auth.getAffiliation();
         this.contactinfo = new ContactInfoRef(auth.getContactInfo());
      }
   }

   public static class ContactInfoRef
   {
      public String email;
      public String phone;

      public ContactInfoRef(){}

      public ContactInfoRef(ContactInfo info)
      {
         this.email = info.getEmail();
         this.phone = info.getPhone();
      }
   }

   public static class PublicationRef
   {
      public Date created;
      public Date modified;

      public PublicationRef(){}

      public PublicationRef(ArticlePublication pub)
      {
         this.created = pub.getCreated();
         this.modified = pub.getModified();
      }
   }
}
