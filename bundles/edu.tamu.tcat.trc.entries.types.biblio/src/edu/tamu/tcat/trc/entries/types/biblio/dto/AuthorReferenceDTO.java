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
package edu.tamu.tcat.trc.entries.types.biblio.dto;

import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;

public class AuthorReferenceDTO
{
   public String authorId;
   public String firstName;
   public String lastName;
   public String role;

   public AuthorReferenceDTO()
   {

   }

   public AuthorReferenceDTO(AuthorReferenceDTO orig)
   {
      this.authorId = orig.authorId;
      this.firstName = orig.firstName;
      this.lastName = orig.lastName;
      this.role = orig.role;
   }

   public static AuthorReferenceDTO create(AuthorReference authorReference)
   {
      AuthorReferenceDTO dto = new AuthorReferenceDTO();

      if (authorReference == null)
      {
         return dto;
      }

      dto.authorId = authorReference.getId();

      String firstName = authorReference.getFirstName();
      String lastName = authorReference.getLastName();

      if (firstName != null && !firstName.trim().isEmpty())
      {
         dto.firstName = firstName.trim();
      }

      if (lastName != null && !lastName.trim().isEmpty())
      {
         dto.lastName = lastName.trim();
      }

      dto.role = authorReference.getRole();

      return dto;
   }
}
