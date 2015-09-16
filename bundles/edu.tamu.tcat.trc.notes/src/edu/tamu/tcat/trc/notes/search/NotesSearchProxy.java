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
package edu.tamu.tcat.trc.notes.search;

import java.util.UUID;

import edu.tamu.tcat.trc.notes.Note;

public class NotesSearchProxy
{

   public String id;
   public String authorId;
   public String associatedEntity;
   public String content;
   public String mimeType;


   public NotesSearchProxy()
   {
   }

   public NotesSearchProxy(Note note)
   {
      this.id = note.getId().toString();

      UUID authorId = note.getAuthorId();
      this.authorId = authorId == null ? null : authorId.toString();
      this.associatedEntity = note.getEntity().toString();
      this.content = note.getContent();
      this.mimeType = note.getMimeType();
   }
}
