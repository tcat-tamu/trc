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
package edu.tamu.tcat.trc.entries.types.biblio.dto.copies;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.EditCopyReferenceCommand;

public abstract class BaseEditCopyRefCmd implements EditCopyReferenceCommand
{

   protected final CopyReference original;
   protected final CopyRefDTO dto;

   public BaseEditCopyRefCmd()
   {
      this.original = null;
      this.dto = new CopyRefDTO();

      // TODO: use IdFactory
      this.dto.id = UUID.randomUUID().toString();
   }

   public BaseEditCopyRefCmd(CopyRefDTO dto)
   {
      this.original = CopyRefDTO.instantiate(dto);
      this.dto = CopyRefDTO.copy(dto);
   }

   @Override
   public final CopyReference getCurrentState()
   {
      return CopyRefDTO.instantiate(dto);
   }

   @Override
   public final String getId()
   {
      return dto.id;
   }

   @Override
   public final void update(CopyRefDTO updates) throws IllegalArgumentException
   {
      if (!isNew())
      {
         if (dto.id != null && updates.id != null && !Objects.equals(dto.id, updates.id))
            throw new IllegalArgumentException("Invalid update: identifiers do not match. "
                  + "Current [" + dto.id + "]. Updates [" + updates.id + "]");
      }

      if (dto.id == null)
         dto.id = updates.id;

      if (updates.type != null)
         setType(updates.type);

      if (updates.associatedEntry != null)
         setAssociatedEntry(updates.associatedEntry);

      if (updates.referenceProperties != null)
         setReferenceProperties(updates.referenceProperties);
      if (updates.title != null)
         setTitle(updates.title);
      if (updates.summary != null)
         setSummary(updates.summary);
      if (updates.rights != null)
         setRights(updates.rights);
   }

   @Override
   public final EditCopyReferenceCommand setType(String type)
   {
      dto.type = type;
      return this;
   }

   @Override
   public final EditCopyReferenceCommand setAssociatedEntry(URI uri)
   {
      dto.associatedEntry = uri;
      return this;
   }

   @Override
   public final EditCopyReferenceCommand setReferenceProperties(Map<String, Object> properties)
   {
      dto.referenceProperties = properties;
      return this;
   }

   @Override
   public final EditCopyReferenceCommand setTitle(String value)
   {
      dto.title = value;
      return this;
   }

   @Override
   public final EditCopyReferenceCommand setSummary(String value)
   {
      dto.summary = value;
      return this;
   }

   @Override
   public final EditCopyReferenceCommand setRights(String description)
   {
      dto.rights = description;
      return this;
   }

   protected final boolean isNew()
   {
      return original == null;
   }

}
