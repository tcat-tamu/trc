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
package edu.tamu.tcat.trc.entries.types.article.repo;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.types.article.dto.ArticleAuthorDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.ArticleDTO;

/**
 *  Used to update properties of an article.
 */
public interface EditArticleCommand
{
   /**
    * @return The unique id for the article that is being edited. Will not be {@code null}
    */
   UUID getId();


   /**
    * Applies all values from a supplied data vehicle to the article being edited.
    *
    * @param article The changes to apply.
    */
   void setAll(ArticleDTO article);

   /**
    * @param title The title of the article.
    */
   void setTitle(String title);

   /**
    * 
    * @param authors The author(s) of the article.
    */
   void setAuthors(List<ArticleAuthorDTO> authors);
   
   /**
    * 
    * @param abs The abstract of the article.
    */
   void setAbstract(String abs);
   
   /**
    * 
    * @param publication The publication date of the article.
    */
   void setPublication(Date publication);
   
   /**
    * 
    * @param modified The last modified date of the article.
    */
   void setLastModified(Date modified);
   
   /**
    * @param entityURI The URI of the entity this article is associated with.
    * @deprecated Need to develop a better strategy for associating with a related entry
    */
   @Deprecated
   void setEntity(URI entityURI);

   /**
    * @param authorId The id of the author responsible for creating this article.
    * @deprecated Need to develop a better model for handling authors.
    */
   @Deprecated
   void setAuthorId(String authorId);

   /**
    * @param mimeType The mime type of the content supplied for this article.
    */
   void setMimeType(String mimeType);

   /**
    * @param content The article's content.
    */
   void setContent(String content);

   /**
    * Executes these updates within the repository.
    *
    * @return The id of the resulting article. If the underlying update fails, the
    *       {@link Future#get()} method will propagate that exception.
    */
   Future<UUID> execute();
}
