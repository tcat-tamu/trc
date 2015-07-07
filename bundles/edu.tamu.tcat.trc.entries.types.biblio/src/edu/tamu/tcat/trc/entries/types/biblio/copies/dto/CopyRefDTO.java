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

public class CopyRefDTO
{
   public UUID id;
   public URI associatedEntry;
   public String copyId;
   public String title;
   public String summary;
   public String rights;

   public static CopyReference instantiate(CopyRefDTO dto)
   {
      return new BasicCopyReference(dto.id, dto.associatedEntry, dto.copyId, dto.title, dto.summary, dto.rights);
   }

   public static CopyRefDTO create(CopyReference ref)
   {
      CopyRefDTO dto = new CopyRefDTO();

      dto.id = ref.getId();
      dto.associatedEntry = ref.getAssociatedEntry();
      dto.copyId = ref.getCopyId();

      dto.title = ref.getTitle();
      dto.summary = ref.getSummary();
      dto.rights = ref.getRights();

      return dto;
   }

   public static CopyRefDTO copy(CopyRefDTO orig)
   {
      CopyRefDTO dto = new CopyRefDTO();

      dto.id = orig.id;
      dto.associatedEntry = orig.associatedEntry;
      dto.copyId = orig.copyId;

      dto.title = orig.title;
      dto.summary = orig.summary;
      dto.rights = orig.rights;

      return dto;
   }
}
