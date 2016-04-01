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
package edu.tamu.tcat.trc.entries.types.biblio.search.solr;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.common.dto.DateDescriptionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorRefDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.PublicationInfoDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.VolumeDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDV;
import edu.tamu.tcat.trc.entries.types.biblio.search.BiblioSearchProxy;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;

/**
 * Represents a document in the SOLR search index. Exports its representation as
 * a {@link SolrInputDocument}, which includes a {@link BiblioSearchProxy} DTO as one of the fields.
 *
 * @see {@link BiblioSearchProxy} which is the DTO stored in one of the fields of this proxy.
 */
public class BiblioDocument
{
   // is this a proxy, mutator or builder
   private final static Logger logger = Logger.getLogger(BiblioDocument.class.getName());

   // composed instead of extended to not expose TrcDocument as API to this class
   private TrcDocument indexDocument;

   public BiblioDocument()
   {
      indexDocument = new TrcDocument(new BiblioSolrConfig());
   }

   public SolrInputDocument getDocument()
   {
      return indexDocument.getSolrDocument();
   }

   public static BiblioDocument createWork(Work work) throws SearchException
   {
      BiblioDocument doc = new BiblioDocument();
      WorkDV workDV = WorkDV.create(work);

      doc.indexDocument.set(BiblioSolrConfig.ID, workDV.id);
      doc.addAuthors(workDV.authors);
      doc.addTitles(workDV.titles);
      doc.indexDocument.set(BiblioSolrConfig.SERIES, workDV.series);
      doc.indexDocument.set(BiblioSolrConfig.SUMMARY, workDV.summary);

      try
      {
         doc.indexDocument.set(BiblioSolrConfig.SEARCH_PROXY, BiblioSearchProxy.create(work));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize BiblioSearchProxy data", e);
      }
      return doc;
   }

   public static BiblioDocument createEdition(String workId, Edition edition) throws SearchException
   {
      EditionDV editionDV = EditionDV.create(edition);
      StringBuilder editionId = new StringBuilder(workId)
                               .append(":")
                               .append(editionDV.id);

      BiblioDocument doc = new BiblioDocument();
      doc.indexDocument.set(BiblioSolrConfig.ID, editionId.toString());
      doc.indexDocument.set(BiblioSolrConfig.EDITION_NAME, editionDV.editionName);
      doc.addAuthors(editionDV.authors);
      doc.addTitles(editionDV.titles);
      doc.addPublication(editionDV.publicationInfo);
      doc.indexDocument.set(BiblioSolrConfig.SERIES, editionDV.series);
      doc.indexDocument.set(BiblioSolrConfig.SUMMARY, editionDV.summary);

      try
      {
         doc.indexDocument.set(BiblioSolrConfig.SEARCH_PROXY, BiblioSearchProxy.create(workId, edition));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize BiblioSearchProxy data", e);
      }
      return doc;
   }

   public static BiblioDocument createVolume(String workId, Edition edition, Volume volume) throws SearchException
   {
      VolumeDV volumeDV = VolumeDV.create(volume);
      StringBuilder volumeId = new StringBuilder(workId)
                              .append(":")
                              .append(edition.getId())
                              .append(":")
                              .append(volumeDV.id);

      BiblioDocument doc = new BiblioDocument();
      doc.indexDocument.set(BiblioSolrConfig.ID, volumeId.toString());
      doc.indexDocument.set(BiblioSolrConfig.EDITION_NAME, edition.getEditionName());
      doc.indexDocument.set(BiblioSolrConfig.VOLUME_NUMBER, volumeDV.volumeNumber);
      doc.addAuthors(volumeDV.authors);
      doc.addTitles(volumeDV.titles);
      doc.addPublication(volumeDV.publicationInfo);
      doc.indexDocument.set(BiblioSolrConfig.SERIES, volumeDV.series);
      doc.indexDocument.set(BiblioSolrConfig.SUMMARY, volumeDV.summary);

      try
      {
         doc.indexDocument.set(BiblioSolrConfig.SEARCH_PROXY, BiblioSearchProxy.create(workId, edition.getId(), volume));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize BiblioSearchProxy data", e);
      }
      return doc;
   }

   public static BiblioDocument updateWork(Work work) throws SearchException
   {
      BiblioDocument doc = new BiblioDocument();
      WorkDV workDV = WorkDV.create(work);

      doc.indexDocument.update(BiblioSolrConfig.ID, workDV.id);
      doc.updateAuthors(workDV.authors);
      doc.updateTitles(workDV.titles);
      doc.indexDocument.update(BiblioSolrConfig.SERIES, workDV.series);
      doc.indexDocument.update(BiblioSolrConfig.SUMMARY, workDV.summary);

      try
      {
         doc.indexDocument.update(BiblioSolrConfig.SEARCH_PROXY, BiblioSearchProxy.create(work));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize BiblioSearchProxy data", e);
      }
      return doc;
   }

   public static BiblioDocument updateEdition(String workId, Edition edition) throws SearchException
   {
      EditionDV editionDV = EditionDV.create(edition);
      StringBuilder editionId = new StringBuilder(workId)
                               .append(":")
                               .append(editionDV.id);

      BiblioDocument doc = new BiblioDocument();
      doc.indexDocument.update(BiblioSolrConfig.ID, editionId.toString());
      doc.indexDocument.update(BiblioSolrConfig.EDITION_NAME, editionDV.editionName);
      doc.updateAuthors(editionDV.authors);
      doc.updateTitles(editionDV.titles);
      doc.updatePublication(editionDV.publicationInfo);
      doc.indexDocument.update(BiblioSolrConfig.SERIES, editionDV.series);
      doc.indexDocument.update(BiblioSolrConfig.SUMMARY, editionDV.summary);

      try
      {
         doc.indexDocument.update(BiblioSolrConfig.SEARCH_PROXY, BiblioSearchProxy.create(workId, edition));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize BiblioSearchProxy data", e);
      }
      return doc;
   }

   public static BiblioDocument updateVolume(String workId, Edition edition, Volume volume) throws SearchException
   {
      VolumeDV volumeDV = VolumeDV.create(volume);
      StringBuilder volumeId = new StringBuilder(workId)
                              .append(":")
                              .append(edition.getId())
                              .append(":")
                              .append(volumeDV.id);

      BiblioDocument doc = new BiblioDocument();
      doc.indexDocument.update(BiblioSolrConfig.ID, volumeId.toString());
      doc.indexDocument.update(BiblioSolrConfig.EDITION_NAME, edition.getEditionName());
      doc.indexDocument.update(BiblioSolrConfig.VOLUME_NUMBER, volumeDV.volumeNumber);
      doc.updateAuthors(volumeDV.authors);
      doc.updateTitles(volumeDV.titles);
      doc.updatePublication(volumeDV.publicationInfo);
      doc.indexDocument.update(BiblioSolrConfig.SERIES, volumeDV.series);
      doc.indexDocument.update(BiblioSolrConfig.SUMMARY, volumeDV.summary);

      try
      {
         doc.indexDocument.update(BiblioSolrConfig.SEARCH_PROXY, BiblioSearchProxy.create(workId, edition.getId(), volume));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize BiblioSearchProxy data", e);
      }
      return doc;
   }

   private void addAuthors(List<AuthorRefDV> authors) throws SearchException
   {
      for (AuthorRefDV author : authors)
      {
         if (author.authorId != null)
            indexDocument.set(BiblioSolrConfig.AUTHOR_IDS, author.authorId);
         else
            indexDocument.set(BiblioSolrConfig.AUTHOR_IDS, "");
         indexDocument.set(BiblioSolrConfig.AUTHOR_NAMES,  author.firstName + " " + author.lastName);
         indexDocument.set(BiblioSolrConfig.AUTHOR_ROLES, author.role);    // not needed
      }
   }

   private void updateAuthors(List<AuthorRefDV> authors) throws SearchException
   {
      Collection<String> allIds = new ArrayList<>();
      Collection<String> allNames = new ArrayList<>();
      Collection<String> allRoles = new ArrayList<>();

      for (AuthorRefDV author : authors)
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
      indexDocument.update(BiblioSolrConfig.AUTHOR_ROLES, allRoles);
   }

   private void addTitles(Collection<TitleDV> titlesDV) throws SearchException
   {
      for (TitleDV title : titlesDV)
      {
         indexDocument.set(BiblioSolrConfig.TITLE_TYPES, title.type);
         indexDocument.set(BiblioSolrConfig.LANGUAGES, title.lg);
         indexDocument.set(BiblioSolrConfig.TITLES, title.title);
         indexDocument.set(BiblioSolrConfig.SUBTITLES, title.subtitle);
      }
   }

   private void updateTitles(Collection<TitleDV> titlesDV) throws SearchException
   {
      Collection<String> allTypes = new ArrayList<>();
      Collection<String> allLangs = new ArrayList<>();
      Collection<String> allTitles = new ArrayList<>();
      Collection<String> allSubTitles = new ArrayList<>();

      for (TitleDV title : titlesDV)
      {
         allTypes.add(title.type);
         allLangs.add(title.lg);
         allTitles.add(title.title);
         allSubTitles.add(title.subtitle);
      }

      indexDocument.update(BiblioSolrConfig.TITLE_TYPES, allTypes);
      indexDocument.update(BiblioSolrConfig.LANGUAGES, allLangs);
      indexDocument.update(BiblioSolrConfig.TITLES, allTitles);
      indexDocument.update(BiblioSolrConfig.SUBTITLES, allSubTitles);
   }

   private void addPublication(PublicationInfoDV publication) throws SearchException
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
      indexDocument.set(BiblioSolrConfig.PUBLICATION_DATE_STRING, dateDescription.description);

      LocalDate pubDate = extractDate(dateDescription.calendar);
      if (pubDate != null)
         indexDocument.set(BiblioSolrConfig.PUBLICATION_DATE, pubDate);
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

   private void updatePublication(PublicationInfoDV publication) throws SearchException
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
      indexDocument.update(BiblioSolrConfig.PUBLICATION_DATE_STRING, dateDescription.description);

      LocalDate pubDate = extractDate(dateDescription.calendar);
      // TODO if null, we need to remove it from the index
      if (pubDate != null)
         indexDocument.update(BiblioSolrConfig.PUBLICATION_DATE, pubDate);

   }
}
