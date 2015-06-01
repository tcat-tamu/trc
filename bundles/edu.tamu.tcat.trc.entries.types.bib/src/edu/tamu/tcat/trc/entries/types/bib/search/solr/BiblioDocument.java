package edu.tamu.tcat.trc.entries.types.bib.search.solr;

import java.time.Year;
import java.time.format.DateTimeFormatter;
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

//   private final static String authorIds = "authorIds";
//   private final static String authorNames = "authorNames";
//   private final static String authorRoles = "authorRole";           // not needed
//   private final static String titleTypes = "titleTypes";            // not needed
//   private final static String language = "lang";                    // not needed
//   private final static String titles = "titles";
//   private final static String subtitles = "subtitles";              // needed? could be joined with titles unless we want to boost separately
//   private final static String publisher = "publisher";
//   private final static String pubLocation = "publisherLocation";
//   private final static String pubDateString = "publishDateString";  // simply date, expressed as a YYYY-MM-DD
//   private final static String docSeries = "series";
//   private final static String docSummary = "summary";               // NOTE - critical search field

//   private final static String editionId = "editionId";
//   private final static String editionName = "editionName";          // not needed (store in info)

//   private final static String volumeId = "volumeId";
//   private final static String volumeNumber = "volumeNumber";        // not needed (store in info)

//   private final static String workInfo = "workInfo";

   // composed instead of extended to not expose TrcDocument as API to this class
   private TrcDocument indexDocument;

   public BiblioDocument()
   {
      indexDocument = new TrcDocument();
   }

   public static BiblioDocument createWork(Work work) throws SearchException
   {
      BiblioDocument doc = new BiblioDocument();
      WorkDV workDV = WorkDV.create(work);

      doc.indexDocument.set(BiblioSolrConfig.ID, workDV.id);
//      doc.addAuthors(workDV.authors);
//      doc.addTitle(workDV.titles);
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
//      doc.addAuthors(editionDV.authors);
//      doc.addTitle(editionDV.titles);
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
//      doc.addAuthors(volumeDV.authors);
//      doc.addTitle(volumeDV.titles);
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
//      doc.addAuthors(workDV.authors);
//      doc.addTitle(workDV.titles);
//      doc.updateField(docSeries, workDV.series, SET);
//      doc.updateField(docSummary, workDV.summary, SET);

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

   public static BiblioDocument updateEdition(String workId, Edition edition) throws SearchException
   {
      EditionDV editionDV = EditionDV.create(edition);
      StringBuilder editionId = new StringBuilder(workId)
                               .append(":")
                               .append(editionDV.id);

      BiblioDocument doc = new BiblioDocument();
      doc.indexDocument.update(BiblioSolrConfig.ID, editionId.toString());
//      doc.updateField(editionName, editionDV.editionName, SET);
//      doc.addAuthors(editionDV.authors);
//      doc.addTitle(editionDV.titles);
      doc.addPublication(editionDV.publicationInfo);
//      doc.updateField(docSeries, editionDV.series, SET);
//      doc.updateField(docSummary, editionDV.summary, SET);

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
//      doc.addAuthors(volumeDV.authors);
//      doc.addTitle(volumeDV.titles);
      doc.addPublication(volumeDV.publicationInfo);
//      doc.updateField(docSeries, volumeDV.series, SET);
//      doc.updateField(docSummary, volumeDV.summary, SET);

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


   public SolrInputDocument getDocument()
   {
      return indexDocument.getSolrDocument();
   }

   private void addAuthors(List<AuthorRefDV> authors) throws SearchException
   {
//      for (AuthorRefDV author : authors)
//      {
//         if (author.authorId != null)
//            document.addField(authorIds, author.authorId);
//         else
//            document.addField(authorIds, "");
//         document.addField(authorNames, author.name);
//         document.addField(authorRoles, author.role);    // not needed
//      }
   }

   private void updateAuthors(List<AuthorRefDV> authors, String updateType) throws SearchException
   {
//      Set<String> allIds = new HashSet<>();
//      Set<String> allNames = new HashSet<>();
//      Set<String> allRoles = new HashSet<>();
//      fieldModifier = new HashMap<>(1);
//
//      for (AuthorRefDV author : authors)
//      {
//         if (author.authorId != null)
//            allIds.add(author.authorId);
//         else
//            document.addField(authorIds, "");
//         allNames.add(author.name);
//         allRoles.add(author.role);
//      }
//
//      fieldModifier.put(updateType, allIds);
//      document.addField(authorIds, fieldModifier);
//
//      fieldModifier.clear();
//      fieldModifier.put(updateType, allNames);
//      document.addField(authorNames, fieldModifier);
//
//      fieldModifier.clear();
//      fieldModifier.put(updateType, allRoles);
//      document.addField(authorRoles, fieldModifier);
   }

   private void addTitle(Collection<TitleDV> titlesDV) throws SearchException
   {
//      for (TitleDV title : titlesDV)
//      {
//         document.addField(titleTypes, title.type);
//         document.addField(language, title.lg);
//         document.addField(titles, title.title);
//         document.addField(subtitles, title.subtitle);
//      }
   }

   private void updateTitle(Collection<TitleDV> titlesDV, String updateType) throws SearchException
   {
//      Set<String> allTypes = new HashSet<>();
//      Set<String> allLangs = new HashSet<>();
//      Set<String> allTitles = new HashSet<>();
//      Set<String> allSubTitles = new HashSet<>();
//      fieldModifier = new HashMap<>(1);
//
//      for (TitleDV title : titlesDV)
//      {
//         allTypes.add(title.type);
//         allLangs.add(title.lg);
//         allTitles.add(title.title);
//         allSubTitles.add(title.subtitle);
//      }
//
//      fieldModifier.put(updateType, allTypes);
//      document.addField(titleTypes, fieldModifier);
//
//      fieldModifier.clear();
//      fieldModifier.put(updateType, allLangs);
//      document.addField(language, fieldModifier);
//
//      fieldModifier.clear();
//      fieldModifier.put(updateType, allTitles);
//      document.addField(titles, fieldModifier);
//
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
         indexDocument.set(BiblioSolrConfig.PUBLICATION_DATE, Year.from(DateTimeFormatter.ISO_DATE.parse(dateDescription.calendar)));
   }

   private void updatePublication(PublicationInfoDV publication, String updateType) throws SearchException
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
         indexDocument.set(BiblioSolrConfig.PUBLICATION_DATE, Year.from(DateTimeFormatter.ISO_DATE.parse(dateDescription.calendar)));
   }
}
