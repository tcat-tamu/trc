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
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorList;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.PublicationInfo;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.TitleDefinition;
import edu.tamu.tcat.trc.entries.types.biblio.Work;


/**
 * Represents a work
 */
public class WorkDV
{
   public String id;
   public String type;
   public List<AuthorRefDV> authors = new ArrayList<>();
   public Collection<TitleDV> titles = new ArrayList<>();
   public List<AuthorRefDV> otherAuthors = new ArrayList<>();
   public String series;
   public String summary;

   // HACK: old records may not have this field; set to empty set by default.
   public Collection<EditionDV> editions = new HashSet<>();

   public static Work instantiate(WorkDV dto)
   {
      BasicWorkImpl work = new BasicWorkImpl();
      work.id = dto.id;

      work.type = dto.type;
      work.authors = AuthorListDV.instantiate(dto.authors);
      work.title = new TitleDefinitionImpl(dto.titles);
      work.otherAuthors = AuthorListDV.instantiate(dto.otherAuthors);
      work.series = dto.series;
      work.summary = dto.summary;
      work.editions = dto.editions.parallelStream()
            .map(EditionDV::instantiate)
            .collect(Collectors.toSet());

      return work;
   }

   public static WorkDV create(Work work)
   {
      WorkDV dto = new WorkDV();

      dto.id = work.getId();
      dto.type = work.getType();
      dto.authors = new ArrayList<>();
      work.getAuthors().forEach(ref -> dto.authors.add(AuthorRefDV.create(ref)));

      dto.otherAuthors = new ArrayList<>();
      work.getOtherAuthors().forEach(ref -> dto.otherAuthors.add(AuthorRefDV.create(ref)));

      Collection<Title> titles = work.getTitle().getAlternateTitles();
      titles.add(work.getTitle().getCanonicalTitle());
      dto.titles = titles.stream().map(TitleDV::create).collect(Collectors.toSet());

      dto.series = work.getSeries();
      dto.summary = work.getSummary();

      dto.editions = work.getEditions().parallelStream()
            .map(EditionDV::create)
            .collect(Collectors.toSet());

      return dto;
   }

   public static class BasicWorkImpl implements Work
   {
         private String id;
         private String type;
         private AuthorList authors;
         private AuthorList otherAuthors;
         private TitleDefinitionImpl title;
         private String series;
         private String summary;
         private Collection<Edition> editions;

         @Override
         public String getId()
         {
            return id;
         }

         @Override
         public String getType()
         {
            return type;
         }

         @Override
         public AuthorList getAuthors()
         {
            return authors;
         }

         @Override
         public TitleDefinition getTitle()
         {
            return title;
         }

         @Override
         public AuthorList getOtherAuthors()
         {
            return otherAuthors;
         }

         @Override
         public PublicationInfo getPublicationInfo()
         {
            throw new UnsupportedOperationException();
         }

         @Override
         public String getSeries()
         {
            return series;
         }

         @Override
         public String getSummary()
         {
            return summary;
         }

         @Override
         public Collection<Edition> getEditions()
         {
            return editions;
         }

         @Override
         public Edition getEdition(String editionId) throws NoSuchCatalogRecordException
         {
            for (Edition edition : editions) {
               if (edition.getId().equals(editionId)) {
                  return edition;
               }
            }

            throw new NoSuchCatalogRecordException("Unable to find edition [" + editionId + "] in work [" + id + "].");
         }

   }
}
