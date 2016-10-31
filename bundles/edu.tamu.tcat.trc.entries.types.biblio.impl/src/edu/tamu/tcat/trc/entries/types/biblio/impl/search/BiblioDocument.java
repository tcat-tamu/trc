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
package edu.tamu.tcat.trc.entries.types.biblio.impl.search;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.DateDescriptionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.PublicationInfoDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.search.BiblioSearchProxy;
import edu.tamu.tcat.trc.search.solr.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;

/**
 * Represents a document in the SOLR search index. Exports its representation as
 * a {@link SolrInputDocument}, which includes a {@link BiblioSearchProxy} DTO as one of the fields.
 *
 * @see {@link BiblioSearchProxy} which is the DTO stored in one of the fields of this proxy.
 */
public class BiblioDocument
{

   // TODO this is of dubious value -- is part of the adapter logic
   // is this a proxy, mutator or builder
   private final static Logger logger = Logger.getLogger(BiblioDocument.class.getName());

   // composed instead of extended to not expose TrcDocument as API to this class
   public final TrcDocument indexDocument;

   public BiblioDocument()
   {
      indexDocument = new TrcDocument(new BiblioSolrConfig());
   }

   public SolrInputDocument getDocument()
   {
      return indexDocument.build();
   }

   public void addAuthors(List<AuthorReferenceDTO> authors) throws SearchException
   {
      for (AuthorReferenceDTO author : authors)
      {
         if (author.authorId != null)
            indexDocument.set(BiblioSolrConfig.AUTHOR_IDS, author.authorId);
         else
            indexDocument.set(BiblioSolrConfig.AUTHOR_IDS, "");
         indexDocument.set(BiblioSolrConfig.AUTHOR_NAMES,  author.firstName + " " + author.lastName);
      }
   }

   public void updateAuthors(List<AuthorReferenceDTO> authors) throws SearchException
   {
      Collection<String> allIds = new ArrayList<>();
      Collection<String> allNames = new ArrayList<>();
      Collection<String> allRoles = new ArrayList<>();

      for (AuthorReferenceDTO author : authors)
      {
         if (author.authorId != null)
            allIds.add(author.authorId);
         else
            indexDocument.update(BiblioSolrConfig.AUTHOR_IDS, "");
         allNames.add( author.firstName + " " + author.lastName);
         allRoles.add(author.role);
      }

      indexDocument.update(BiblioSolrConfig.AUTHOR_IDS, allIds);
      indexDocument.update(BiblioSolrConfig.AUTHOR_NAMES, allNames);
   }

   public void addTitles(Collection<TitleDTO> titlesDV) throws SearchException
   {
      for (TitleDTO title : titlesDV)
      {
         indexDocument.set(BiblioSolrConfig.TITLES, title.title);
      }
   }

   public void updateTitles(Collection<TitleDTO> titlesDV) throws SearchException
   {
      Collection<String> allTitles = new ArrayList<>();

      for (TitleDTO title : titlesDV)
      {
         allTitles.add(title.title);
      }

      indexDocument.update(BiblioSolrConfig.TITLES, allTitles);
   }

   public void addPublication(PublicationInfoDTO publication) throws SearchException
   {
      if (publication.publisher != null)
         indexDocument.set(BiblioSolrConfig.PUBLISHER, publication.publisher);
      else
         indexDocument.set(BiblioSolrConfig.PUBLISHER, "");

      if (publication.place != null)
         indexDocument.set(BiblioSolrConfig.PUBLISHER_LOCATION, publication.place);
      else
         indexDocument.set(BiblioSolrConfig.PUBLISHER_LOCATION, "");

      DateDescriptionDTO dateDescription = publication.date;
      if (dateDescription != null)
      {
         LocalDate pubDate = extractDate(dateDescription.calendar);
         if (pubDate != null)
            indexDocument.set(BiblioSolrConfig.PUBLICATION_DATE, pubDate);
      }
   }

   private LocalDate extractDate(String calendar)
   {
      // TODO Auto-generated method stub
      if (calendar == null || calendar.trim().isEmpty())
         return null;

      try
      {
         return LocalDate.from(DateTimeFormatter.ISO_DATE.parse(calendar));
      }
      catch (DateTimeParseException dtpe)
      {
         logger.log(Level.WARNING, "Failed to parse supplied publication date: " + calendar + ". This date will not be avialable for indexing", dtpe);
         return null;
      }
   }

   public void updatePublication(PublicationInfoDTO publication) throws SearchException
   {
      if (publication.publisher != null)
         indexDocument.update(BiblioSolrConfig.PUBLISHER, publication.publisher);
      else
         indexDocument.update(BiblioSolrConfig.PUBLISHER, "");

      if (publication.place != null)
         indexDocument.update(BiblioSolrConfig.PUBLISHER_LOCATION, publication.place);
      else
         indexDocument.update(BiblioSolrConfig.PUBLISHER_LOCATION, "");

      DateDescriptionDTO dateDescription = publication.date;
      if (dateDescription != null)
      {
          LocalDate pubDate = extractDate(dateDescription.calendar);
          // TODO if null, we need to remove it from the index
          if (pubDate != null)
             indexDocument.update(BiblioSolrConfig.PUBLICATION_DATE, pubDate);
       }
   }
}
