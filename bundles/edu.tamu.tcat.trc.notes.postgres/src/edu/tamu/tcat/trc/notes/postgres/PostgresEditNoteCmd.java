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
package edu.tamu.tcat.trc.notes.postgres;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import edu.tamu.tcat.trc.notes.dto.NoteDTO;
import edu.tamu.tcat.trc.notes.repo.EditNoteCommand;

public class PostgresEditNoteCmd implements EditNoteCommand
{

   private final NoteDTO note;
   private final AtomicBoolean executed = new AtomicBoolean(false);

   private Function<NoteDTO, Future<UUID>> commitHook;

   public PostgresEditNoteCmd(NoteDTO note)
   {
      this.note = note;
   }

   public void setCommitHook(Function<NoteDTO, Future<UUID>> hook)
   {
      commitHook = hook;
   }

   @Override
   public UUID getId()
   {
      return note.id;
   }

   @Override
   public void setAll(NoteDTO updateDTO)
   {
      if (updateDTO.id != null && !updateDTO.id.equals(note.id))
         throw new IllegalArgumentException("The supplied note ");

      note.associatedEntity = updateDTO.associatedEntity;
      note.authorId = updateDTO.authorId;
      note.content = updateDTO.content;
      note.mimeType = updateDTO.mimeType;
   }

   @Override
   public void setEntity(URI entityURI)
   {
      note.associatedEntity = entityURI;
   }

   @Override
   public void setAuthorId(String authorId)
   {
      note.authorId = authorId;
   }

   @Override
   public void setMimeType(String mimeType)
   {
      note.mimeType = mimeType;
   }

   @Override
   public void setContent(String content)
   {
      note.content = content;
   }

   @Override
   public Future<UUID> execute()
   {
      Objects.requireNonNull(commitHook, "No commit hook supplied.");
      if (!executed.compareAndSet(false, true))
         throw new IllegalStateException("This edit copy command has already been invoked.");

      return commitHook.apply(note);
   }

}
