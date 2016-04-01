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
package edu.tamu.tcat.trc.entries.types.biblio.rest.v1.copies;

import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.copies.dto.CopyRefDTO;

/**
 * An encapsulation of adapter methods to convert between the repository API and
 * the {@link RestApiV1} schema DTOs.
 */
public class RepoAdapter
{
   public static RestApiV1.CopyReference toDTO(CopyReference orig)
   {
      if (orig == null)
         return null;
      
      RestApiV1.CopyReference dto = new RestApiV1.CopyReference();
      dto.id = orig.getId();
      dto.associatedEntry = orig.getAssociatedEntry();
      dto.copyId = orig.getCopyId();

      dto.title = orig.getTitle();
      dto.summary = orig.getSummary();
      dto.rights = orig.getRights();
      
      return dto;
   }
   
   public static CopyRefDTO toRepo(RestApiV1.CopyReference orig)
   {
      if (orig == null)
         return null;
      
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
