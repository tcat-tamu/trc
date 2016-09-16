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
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.NoSuchEntryException;
import edu.tamu.tcat.trc.entries.types.article.Article;


public interface ArticleRepository extends EntryRepository<Article>
{

   /** The type id used to identify articles within the EntryResolver framework. */
   public final static String ENTRY_TYPE_ID = "trc.entries.article";

   /** The initial path (relative to some API endpoint) for building URIs that reference
    *  and article and its sub-elements. */
   public final static String ENTRY_URI_BASE = "entries/articles/";

   /**
    * Retrieves a specific {@link Article}
    *
    * @param articleId The id of the Article to retrieve
    * @return The identified article.
    * @throws NoSuchEntryException If the requested article does not exist.
    */
   @Override
   Article get(String articleId) throws NoSuchEntryException;

   /**
    * Retrieves a list of {@link Article} associated with a particular URI.
    *
    * @param entityURI URI that may contain an {@link Article}.
    * @return Collection of Articles
    */
   // NOTE that this should, perhaps, be done through the search API.
   List<Article> getArticles(URI entityURI) throws NoSuchEntryException;

   /**
    * Builds a new {@link EditArticleCommand} to create a new {@link Article}.
    * @return
    */
   @Override
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
   @Override
   EditArticleCommand create(String id);

   /**
    * Modifies a {@link ArticleNoteCommand} to allow editing a {@link Article}.
    * @return
    */
   @Override
   EditArticleCommand edit(String articleId) throws NoSuchEntryException;

   /**
    * Removes a {@link Article} entry from the database.
    */
   @Override
   CompletableFuture<Boolean> remove(String articleId);

   /**
    * Register a observer that will be notified when an article changes.
    *
    * @param observer The observer to be notified.
    * @return A registration that allows the client to stop listening for changes. The returned
    *       registration <em>must</em> be closed by the caller.
    */
   @Override
   EntryRepository.ObserverRegistration onUpdate(EntryRepository.UpdateObserver<Article> observer);
}
