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

import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorRefDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDV;

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

   public SimpleWorkDV(WorkDV works)
   {
      this.id = works.id;
      for (AuthorRefDV author : works.authors)
      {
         authorIds.add(author.authorId);
         authorNames.add(author.name);
         authorRole.add(author.role);
      }

      for (TitleDV title : works.titles)
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
