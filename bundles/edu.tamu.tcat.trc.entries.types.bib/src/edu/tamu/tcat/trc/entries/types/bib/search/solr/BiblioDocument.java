package edu.tamu.tcat.trc.entries.types.bib.search.solr;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.common.dto.DateDescriptionDTO;
import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.search.solr.impl.TrcDocument;
import edu.tamu.tcat.trc.entries.types.bib.Edition;
import edu.tamu.tcat.trc.entries.types.bib.Volume;
import edu.tamu.tcat.trc.entries.types.bib.Work;
import edu.tamu.tcat.trc.entries.types.bib.dto.AuthorRefDV;
import edu.tamu.tcat.trc.entries.types.bib.dto.EditionDV;
import edu.tamu.tcat.trc.entries.types.bib.dto.PublicationInfoDV;
import edu.tamu.tcat.trc.entries.types.bib.dto.TitleDV;
import edu.tamu.tcat.trc.entries.types.bib.dto.VolumeDV;
import edu.tamu.tcat.trc.entries.types.bib.dto.WorkDV;
import edu.tamu.tcat.trc.entries.types.bib.search.BiblioSearchProxy;

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
//      doc.addField(docSeries, workDV.series);
//      doc.addField(docSummary, workDV.summary);

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
//      doc.addField(editionName, editionDV.editionName);
      doc.addAuthors(editionDV.authors);
      doc.addTitles(editionDV.titles);
      doc.addPublication(editionDV.publicationInfo);
//      doc.addField(docSeries, editionDV.series);
//      doc.addField(docSummary, editionDV.summary);

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
//      doc.addField(editionName, edition.getEditionName());
//      doc.addField(volumeNumber, volumeDV.volumeNumber);
      doc.addAuthors(volumeDV.authors);
      doc.addTitles(volumeDV.titles);
      doc.addPublication(volumeDV.publicationInfo);
//      doc.addField(docSeries, volumeDV.series);
//      doc.addField(docSummary, volumeDV.summary);

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
//      doc.updateField(docSeries, workDV.series, SET);
//      doc.updateField(docSummary, workDV.summary, SET);

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
//      doc.updateField(editionName, editionDV.editionName, SET);
      doc.updateAuthors(editionDV.authors);
      doc.updateTitles(editionDV.titles);
      doc.updatePublication(editionDV.publicationInfo);
//      doc.updateField(docSeries, editionDV.series, SET);
//      doc.updateField(docSummary, editionDV.summary, SET);

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
//      doc.updateField(editionName, edition.getEditionName(), SET);
//      doc.updateField(volumeNumber, volumeDV.volumeNumber, SET);
      doc.updateAuthors(volumeDV.authors);
      doc.updateTitles(volumeDV.titles);
      doc.updatePublication(volumeDV.publicationInfo);
//      doc.updateField(docSeries, volumeDV.series, SET);
//      doc.updateField(docSummary, volumeDV.summary, SET);

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
//         if (author.authorId != null)
//            document.addField(authorIds, author.authorId);
//         else
//            document.addField(authorIds, "");
         indexDocument.set(BiblioSolrConfig.AUTHOR_NAMES, author.name);
//         document.addField(authorRoles, author.role);    // not needed
      }
   }

   private void updateAuthors(List<AuthorRefDV> authors) throws SearchException
   {
//      Collection<String> allIds = new ArrayList<>();
      Collection<String> allNames = new ArrayList<>();
//      Collection<String> allRoles = new ArrayList<>();
//      fieldModifier = new HashMap<>(1);

      for (AuthorRefDV author : authors)
      {
//         if (author.authorId != null)
//            allIds.add(author.authorId);
//         else
//            document.addField(authorIds, "");
         allNames.add(author.name);
//         allRoles.add(author.role);
      }

//      fieldModifier.put(updateType, allIds);
//      document.addField(authorIds, fieldModifier);

      indexDocument.update(BiblioSolrConfig.AUTHOR_NAMES, allNames);

//      fieldModifier.clear();
//      fieldModifier.put(updateType, allRoles);
//      document.addField(authorRoles, fieldModifier);
   }

   private void addTitles(Collection<TitleDV> titlesDV) throws SearchException
   {
      for (TitleDV title : titlesDV)
      {
//         document.addField(titleTypes, title.type);
//         document.addField(language, title.lg);
         indexDocument.set(BiblioSolrConfig.TITLES, title.title);
//         document.addField(subtitles, title.subtitle);
      }
   }

   private void updateTitles(Collection<TitleDV> titlesDV) throws SearchException
   {
//      Collection<String> allTypes = new ArrayList<>();
//      Collection<String> allLangs = new ArrayList<>();
      Collection<String> allTitles = new ArrayList<>();
//      Collection<String> allSubTitles = new ArrayList<>();
//      fieldModifier = new HashMap<>(1);
//
      for (TitleDV title : titlesDV)
      {
//         allTypes.add(title.type);
//         allLangs.add(title.lg);
         allTitles.add(title.title);
//         allSubTitles.add(title.subtitle);
      }

//      fieldModifier.put(updateType, allTypes);
//      document.addField(titleTypes, fieldModifier);
//
//      fieldModifier.clear();
//      fieldModifier.put(updateType, allLangs);
//      document.addField(language, fieldModifier);

      indexDocument.update(BiblioSolrConfig.TITLES, allTitles);

//      fieldModifier.clear();
//      fieldModifier.put(updateType, allSubTitles);
//      document.addField(subtitles, fieldModifier);
   }

   private void addPublication(PublicationInfoDV publication) throws SearchException
   {
//      if (publication.publisher != null)
//         document.addField(publisher, publication.publisher);
//      else
//         document.addField(publisher, "");
//      if (publication.place != null)
//         document.addField(pubLocation, publication.place);
//      else
//         document.addField(pubLocation, "");

      DateDescriptionDTO dateDescription = publication.date;
//      document.addField(pubDateString, dateDescription.description);

      if (dateDescription.calendar != null)
         indexDocument.set(BiblioSolrConfig.PUBLICATION_DATE, LocalDate.from(DateTimeFormatter.ISO_DATE.parse(dateDescription.calendar)));
   }

   private void updatePublication(PublicationInfoDV publication) throws SearchException
   {
//      if (publication.publisher != null)
//         document.addField(publisher, publication.publisher);
//      else
//         document.addField(publisher, "");
//      if (publication.place != null)
//         document.addField(pubLocation, publication.place);
//      else
//         document.addField(pubLocation, "");
//
      DateDescriptionDTO dateDescription = publication.date;
//      document.addField(pubDateString, dateDescription.description);

      if (dateDescription.calendar != null)
         indexDocument.update(BiblioSolrConfig.PUBLICATION_DATE, LocalDate.from(DateTimeFormatter.ISO_DATE.parse(dateDescription.calendar)));
   }
}
