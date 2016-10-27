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
package edu.tamu.tcat.trc.services.notes;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.repo.EditEntryCommand;
import edu.tamu.tcat.trc.resolver.EntryId;

/**
 *  Encapsulates a single transaction for editing a note (either newly created or existing).
 */
public interface EditNoteCommand extends EditEntryCommand<Note>
{
   /**
    * @return The unique id for the note that is being edited. Will not be {@code null}
    */
   String getId();

   /**
    * @param entityURI The URI of the entity this note is associated with.
    */
   void setAssociatedEntry(EntryId ref);

   /**
    * @param authorId The id of the author responsible for creating this note.
    */
   void setAuthor(Account account);

   /**
    * @param mimeType The mime type of the content supplied for this note.
    */
   void setMimeType(String mimeType);

   /**
    * @param content The note's content.
    */
   void setContent(String content);

   /**
    * Executes these updates within the repository.
    *
    * @return The id of the resulting note. If the underlying update fails, the
    *       {@link Future#get()} method will propagate that exception.
    */
   CompletableFuture<Note> exec();

   @Override
   default CompletableFuture<String> execute()
   {
      return this.exec().thenApply(n -> n.getId());
   }
}
