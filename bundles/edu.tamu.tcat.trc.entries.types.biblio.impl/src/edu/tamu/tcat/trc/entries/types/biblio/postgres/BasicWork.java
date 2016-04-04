package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorList;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.PublicationInfo;
import edu.tamu.tcat.trc.entries.types.biblio.TitleDefinition;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;

public class BasicWork implements Work
{
   private final String id;
   private final AuthorList authors;
   private final TitleDefinition title;
   private final AuthorList otherAuthors;
   private final List<Edition> editions;
   private final String series;
   private final String summary;
   private final CopyReference defaultCopyReference;
   private final Set<CopyReference> copyReferences;

   public BasicWork(String id,
                    AuthorList authors,
                    TitleDefinition title,
                    AuthorList otherAuthors,
                    List<Edition> editions,
                    String series,
                    String summary,
                    CopyReference defaultCopyReference,
                    Set<CopyReference> copyReferences)
   {
      this.id = id;

      // TODO: should copies be created of aggregate types?
      this.authors = authors;
      this.title = title;
      this.otherAuthors = otherAuthors;

      // ensure editions are sorted properly
      editions.sort(Comparator.comparing(BasicWork::extractPublicationDate));
      this.editions = editions;

      this.series = series;
      this.summary = summary;
      this.defaultCopyReference = defaultCopyReference;
      this.copyReferences = copyReferences;
   }

   @Override
   public String getId()
   {
      return id;
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
   public List<Edition> getEditions()
   {
      return editions;
   }

   @Override
   public Edition getEdition(String editionId)
   {
      for (Edition edition : editions) {
         if (edition.getId().equals(editionId)) {
            return edition;
         }
      }

      return null;
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
   public CopyReference getDefaultCopyReference()
   {
      return defaultCopyReference;
   }

   @Override
   public Set<CopyReference> getCopyReferences()
   {
      return copyReferences;
   }

   /**
    * Extract publication date information from an edition.
    *
    * @param edition
    * @return
    */
   private static String extractPublicationDate(Edition edition) {
      String editionName = edition.getEditionName() == null ? "" : edition.getEditionName();
      PublicationInfo publicationInfo = edition.getPublicationInfo();

      if (publicationInfo == null) {
         return editionName;
      }

      DateDescription publicationDate = publicationInfo.getPublicationDate();

      if (publicationDate == null) {
         return editionName;
      }

      LocalDate date = publicationDate.getCalendar();

      if (date == null) {
         return editionName;
      }

      return date.toString();
   }
}