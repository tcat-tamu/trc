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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.Title;

/**
 * A helper class to manage fields that are common across different levels of the Work taxonomy.
 *
 */
public class CommonFieldsDelegate
{

   private List<AuthorReference> authors = new ArrayList<>();
   private Collection<Title> titles = new HashSet<>();
   private List<AuthorReference> otherAuthors = new ArrayList<>();

   private String summary;

   public CommonFieldsDelegate()
   {
      // TODO Auto-generated constructor stub
   }
   public CommonFieldsDelegate(List<AuthorRefDV> authors, Collection<TitleDV> titles, List<AuthorRefDV> others, String summary)
   {

      if (authors != null)
      {
         this.authors = authors.stream()
               .map(AuthorRefDV::instantiate)
               .collect(Collectors.toList());
      }

      if (titles != null)
      {
         this.titles = titles.parallelStream()
               .map(TitleDV::instantiate)
               .collect(Collectors.toSet());
      }

      if (others != null)
      {
         this.otherAuthors = others.stream()
               .map(AuthorRefDV::instantiate)
               .collect(Collectors.toList());
      }

      this.summary = summary;
   }

   public List<AuthorReference> getAuthors()
   {
      return Collections.unmodifiableList(authors);
   }

   public Collection<Title> getTitles()
   {
      return Collections.unmodifiableCollection(titles);
   }

   public List<AuthorReference> getOtherAuthors()
   {
      return Collections.unmodifiableList(otherAuthors);
   }

   public String getSummary()
   {
      return summary;
   }
}
