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

import java.time.ZonedDateTime;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;

public interface Note
{
   /**
    * @return A unique identifier for this note.
    */
   String getId();

   /**
    * @return The time this entry was created.
    */
   ZonedDateTime getDateCreated();

   /**
    * @return The time this entry was last modified.
    */
   ZonedDateTime getDateModified();

   /**
    * @return A reference to the entry this note is associated with. May be
    *       <code>null</code> if the note is not attached to a specific entry.
    */
   EntryReference getAssociatedEntry();

   // TODO allow for attachments to multiple entries

   /**
    * Returns the account of the user responsible for creating this note.
    *
    * @return An application defined unique identifier for the author. May be
    *       <code>null</code> if no author is identified.
    */
   Account getAuthor();

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
