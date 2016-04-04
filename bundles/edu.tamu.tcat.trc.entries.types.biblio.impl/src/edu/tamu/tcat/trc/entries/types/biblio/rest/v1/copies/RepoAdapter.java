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
import edu.tamu.tcat.trc.entries.types.biblio.dto.copies.CopyReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.EditCopyReferenceCommand;

/**
 * An encapsulation of adapter methods to convert between the repository API and
 * the {@link RestApiV1} schema DTOs.
 */
public class RepoAdapter
{
   public static void save(RestApiV1.CopyReference dto, EditCopyReferenceCommand command)
   {
      command.setType(dto.type);
      command.setProperties(dto.properties);
      command.setTitle(dto.title);
      command.setSummary(dto.summary);
      command.setRights(dto.rights);
   }

   public static RestApiV1.CopyReference toDTO(CopyReference copyReference)
   {
      if (copyReference == null)
      {
         return null;
      }

      RestApiV1.CopyReference dto = new RestApiV1.CopyReference();

      dto.id = copyReference.getId();
      dto.type = copyReference.getType();
      dto.properties = copyReference.getProperties();
      dto.title = copyReference.getTitle();
      dto.summary = copyReference.getSummary();
      dto.rights = copyReference.getRights();

      return dto;
   }

   public static CopyReferenceDTO toRepo(RestApiV1.CopyReference restDto)
   {
      if (restDto == null)
      {
         return null;
      }

      CopyReferenceDTO repoDto = new CopyReferenceDTO();

      repoDto.id = restDto.id;
      repoDto.type = restDto.type;
      repoDto.properties = restDto.properties;
      repoDto.title = restDto.title;
      repoDto.summary = restDto.summary;
      repoDto.rights = restDto.rights;

      return repoDto;
   }
}
