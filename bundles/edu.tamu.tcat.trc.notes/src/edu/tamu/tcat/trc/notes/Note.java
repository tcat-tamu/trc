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
package edu.tamu.tcat.trc.notes;

import java.net.URI;
import java.util.UUID;

public interface Note
{
   public enum NotesMimeType
   {
      TEXT,
      HTML
   }

   /**
    * @return A unique identifier for this note.
    */
   UUID getId();

   /**
    * @return URI of the entity to which they are attached. Note that this may be an element
    *    within a catalog entry (e.g., attached to an author reference) once the entry type
    *    itself provides well-defined URIs for those internal components.
    */
   URI getEntity();

   /**
    * @return An application defined unique identifier for the author.
    */
   UUID getAuthorId();

   /**
    * @return The type of content for the note. Currently we anticipate only
    *    text or HTML notes but applications may provide support for other data types
    *    (e.g. Markdown, XML, SVG, etc) provided that they can be represented as contents
    *    of a JSON document.
    */
   String getMimeType();

   /**
    * @return The content of the note.
    */
   String getContent();
}
