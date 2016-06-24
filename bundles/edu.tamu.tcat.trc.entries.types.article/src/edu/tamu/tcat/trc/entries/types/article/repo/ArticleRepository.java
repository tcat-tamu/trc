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
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.article.Article;


public interface ArticleRepository
{
   /**
    * Retrieves a specific {@link Article}
    *
    * @param articleId The id of the Article to retrieve
    * @return The identified article.
    * @throws NoSuchCatalogRecordException If the requested article does not exist.
    */
   Article get(String articleId) throws NoSuchCatalogRecordException;

   /**
    * Retrieves a list of {@link Article} associated with a particular URI.
    *
    * @param entityURI URI that may contain an {@link Article}.
    * @return Collection of Articles
    */
   // NOTE that this should, perhaps, be done through the search API.
   List<Article> getArticles(URI entityURI) throws NoSuchCatalogRecordException;

   /**
    * Builds a new {@link EditArticleCommand} to create a new {@link Article}.
    * @return
    */
   EditArticleCommand create();

   /**
    * Constructs an {@link EditArticleCommand} to create a new article with the
    * specified id. It is the responsibility of the caller to ensure the uniqueness
    * of the id. If the id is not unique, the execution of the command will fail.
    *
    * @param id The id of the article to create.
    * @return A command to edit the article. Note the article will not be created
    *    until the returned command is executed.
    */
   EditArticleCommand create(String id);

   /**
    * Modifies a {@link ArticleNoteCommand} to allow editing a {@link Article}.
    * @return
    */
   EditArticleCommand edit(String articleId) throws NoSuchCatalogRecordException;

   /**
    * Removes a {@link Article} entry from the database.
    */
   Future<Boolean> remove(String articleId);

   /**
    * Register a listener that will be notified when an article changes.
    *
    * @param ears The listener to be notified.
    * @return A registration that allows the client to stop listening for changes. The returned
    *       registration <em>must</em> be closed by the caller.
    */
   public Runnable register(Consumer<Article> ears);


}
