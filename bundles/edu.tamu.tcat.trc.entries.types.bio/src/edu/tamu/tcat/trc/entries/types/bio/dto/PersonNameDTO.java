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
package edu.tamu.tcat.trc.entries.types.bio.dto;

import edu.tamu.tcat.trc.entries.types.bio.PersonName;

public class PersonNameDTO
{
   public String title;
   public String givenName;
   public String middleName;
   public String familyName;
   public String suffix;

   public String displayName;

   /**
    * Create a new data vehicle from the supplied {@link PersonName}.
    */
   public static PersonNameDTO create(PersonName name)
   {
      PersonNameDTO dto = new PersonNameDTO();

      dto.title = name.getTitle();
      dto.givenName = name.getGivenName();
      dto.middleName = name.getMiddleName();
      dto.familyName = name.getFamilyName();
      dto.suffix = name.getSuffix();

      dto.displayName = name.getDisplayName();

      return dto;
   }


   @Deprecated
   public static PersonNameImpl instantiate(PersonNameDTO personDV)
   {
      PersonNameImpl name = new PersonNameImpl();
      name.title = personDV.title;
      name.givenName = personDV.givenName;
      name.middleName = personDV.middleName;
      name.familyName = personDV.familyName;
      name.suffix = personDV.suffix;

      name.displayName = personDV.displayName;

      return name;
   }


   @Deprecated
   public static class PersonNameImpl implements PersonName
   {
      private String title;
      private String givenName;
      private String middleName;
      private String familyName;
      private String suffix;

      private String displayName;


      @Override
      public String getTitle()
      {
         return title;
      }

      @Override
      public String getGivenName()
      {
         return givenName;
      }

      @Override
      public String getMiddleName()
      {
         return middleName;
      }

      @Override
      public String getFamilyName()
      {
         return familyName;
      }

      @Override
      public String getSuffix()
      {
         return suffix;
      }

      @Override
      public String getDisplayName()
      {
         return displayName;
      }

   }
}
