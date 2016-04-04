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
package edu.tamu.tcat.trc.entries.types.biblio.search;

import java.util.List;
import java.util.StringJoiner;

import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;

public class SimpleWorkDV
{
   // FIXME this is internal to the search system. Does not belong here.

   public String id;
   public List<String> authorIds;
   public List<String> authorNames;
   public List<String> authorRole;
   public List<String> titleTypes;
   public List<String> lang;
   public List<String> titles;
   public List<String> subtitles;
   public String series;
   public String summary;

   public String _version_;

   public SimpleWorkDV()
   {

   }

   public SimpleWorkDV(WorkDTO works)
   {
      this.id = works.id;
      for (AuthorReferenceDTO author : works.authors)
      {
         authorIds.add(author.authorId);
         authorRole.add(author.role);

         String firstName = author.firstName;
         String lastName = author.lastName;

         StringJoiner sj = new StringJoiner(" ");

         if (firstName != null && !firstName.trim().isEmpty())
         {
            sj.add(firstName.trim());
         }

         if (lastName != null && !lastName.trim().isEmpty())
         {
            sj.add(lastName.trim());
         }

         authorNames.add(sj.toString());
      }

      for (TitleDTO title : works.titles)
      {
         titles.add(title.title);
         subtitles.add(title.subtitle);
         lang.add(title.lg);
         titleTypes.add(title.type);
      }

      this.series = works.series;
      this.summary = works.summary;

      this._version_ = "";
   }
}
