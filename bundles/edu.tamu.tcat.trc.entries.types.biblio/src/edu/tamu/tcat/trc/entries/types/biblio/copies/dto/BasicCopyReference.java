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
package edu.tamu.tcat.trc.entries.types.biblio.copies.dto;

import java.net.URI;
import java.util.UUID;

import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;

public class BasicCopyReference implements CopyReference
{
   private final UUID id;
   private final URI associatedEntry;
   private final String copyId;
   private final String title;
   private final String summary;
   private final String rights;

   public BasicCopyReference(UUID id, URI entry, String copyId, String title, String summary, String rights)
   {
      this.id = id;
      this.associatedEntry = entry;
      this.copyId = copyId;

      this.title = title != null ? title : "";
      this.summary = summary != null ? summary : "";
      this.rights = rights != null ? rights : "";
   }

   @Override
   public UUID getId()
   {
      return id;
   }
   @Override
   public URI getAssociatedEntry()
   {
      return associatedEntry;
   }
   @Override
   public String getCopyId()
   {
      return copyId;
   }
   @Override
   public String getTitle()
   {
      return title;
   }
   @Override
   public String getSummary()
   {
      return summary;
   }
   @Override
   public String getRights()
   {
      return rights;
   }
}
