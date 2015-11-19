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
package edu.tamu.tcat.trc.entries.types.article.dto;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;

public class ArticleDTO
{
   public UUID id;
   public String title;
   public List<ArticleAuthorDTO> authors;
   public String articleAbstract;
   public Date publication;
   public Date lastModified;
   public URI associatedEntity;
   public String authorId;
   public String mimeType;
   public String content;
   public String slug;

   public static ArticleDTO create(Article article)
   {
      ArticleDTO dto = new ArticleDTO();
      UUID author = article.getAuthorId();

      dto.id = article.getId();
      dto.title = article.getTitle();
      dto.authors = convertAuthors(article.getAuthors());
      dto.slug = article.getSlug();
      dto.articleAbstract = article.getAbstract();
      dto.publication = article.getPublishedDate();
      dto.lastModified = article.getLastModified();
      dto.associatedEntity = article.getEntity();
      dto.authorId = author != null ? author.toString() : "";
      dto.mimeType = article.getMimeType();
      dto.content = article.getContent();

      return dto;
   }

   public static ArticleDTO copy(ArticleDTO orig)
   {
      ArticleDTO dto = new ArticleDTO();

      dto.id = orig.id;
      dto.title = orig.title;
      dto.authors = new ArrayList<ArticleAuthorDTO>(orig.authors);
      dto.slug = orig.slug;
      dto.publication = orig.publication;
      dto.lastModified = orig.lastModified;
      dto.associatedEntity = orig.associatedEntity;
      dto.authorId = orig.authorId;
      dto.mimeType = orig.mimeType;
      dto.content = orig.content;

      return dto;
   }
   
   private static List<ArticleAuthorDTO> convertAuthors(List<ArticleAuthor> authors)
   {
      List<ArticleAuthorDTO> auths = new ArrayList<>();
      
      authors.forEach((a) ->
      {
         ArticleAuthorDTO authDto = ArticleAuthorDTO.create(a);
         authDto.id = a.getId();
         authDto.name = a.getName();
         authDto.affiliation = a.getAffiliation();
         authDto.email = a.getEmail();
         authDto.contactOther = a.getOther();
         
         auths.add(authDto);
      });
      
      return auths;
   }
}
