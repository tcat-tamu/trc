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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.core.repo.EditEntryCommand;
import edu.tamu.tcat.trc.entries.types.article.Article;

/**
 *  Used to update properties of an article.
 */
public interface EditArticleCommand extends EditEntryCommand<Article>
{
   /**
    * @return The unique id for the article that is being edited. Will not be {@code null}
    */
   String getId();

   /**
    * @param mimeType The mime type of the content supplied for this article.
    */
   void setContentType(String mimeType);

   /**
    * @param type An application defined type for this article.
    */
   void setArticleType(String type);

   /**
    * @param title The title of the article.
    */
   void setTitle(String title);

   /**
    * @param slug the slug for this article. Note that slugs must be unique.
    */
   void setSlug(String slug);

   /**
    * Adds an author with the specified id to this article.
    *
    * @param authorId The id of the author to add.
    * @return A mutator to be used to edit properties about this author.
    */
   AuthorMutator addAuthor(String authorId);

   /**
    * Edits an existing author.
    *
    * @param authorId The id of the author to edit.
    * @return A mutator to be used to edit properties about this author.
    */
   AuthorMutator editAuthor(String authorId);

   /**
    * Moves the identified author to a position in front of the target author.
    *
    * @param authorId The id of the author to move.
    * @param beforeId The id of the author to move in front of. If {@code null} or
    *       not found the author will be moved to the end of the list.
    */
   void moveAuthor(String authorId, String beforeId);

   /**
    * Removes the indicated author.
    * @param authorId The id of the author to remove.
    */
   void removeAuthor(String authorId);

   /**
    *
    * @param abs The abstract of the article.
    */
   void setAbstract(String abs);

   /**
    *
    * @param body The main text of the article.
    */
   void setBody(String body);

   /**
    * Removes all footnotes from the article
    */
   void clearFootnotes();

   /**
    * Edits an existing footnote with the given id or creates a new footnote with the given id.
    * @param id
    * @return A mutator for the footnote
    */
   FootnoteMutator editFootnote(String id);

   /**
    * Executes these updates within the repository.
    *
    * @return The id of the resulting article. If the underlying update fails, the
    *       {@link Future#get()} method will propagate that exception.
    */
   @Override
   CompletableFuture<String> execute();

}
