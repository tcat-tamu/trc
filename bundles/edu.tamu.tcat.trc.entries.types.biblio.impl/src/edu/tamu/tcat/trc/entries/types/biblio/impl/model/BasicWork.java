package edu.tamu.tcat.trc.entries.types.biblio.impl.model;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorList;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.PublicationInfo;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.TitleDefinition;

public class BasicWork implements BibliographicEntry
{
   private final String id;
   @Deprecated // see note on Work#getType()
   private final String type;
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
      this(id,
           null,
           authors,
           title,
           otherAuthors,
           editions,
           series,
           summary,
           defaultCopyReference,
           copyReferences);
   }

   public BasicWork(String id,
                    String type,
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

      this.type = type;

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
   @Deprecated
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
   public Optional<Title> getTitle(String... types)
   {
      TreeSet<Title> titles = new TreeSet<>(BasicWork::compare);
      titles.addAll(getTitle().get());

      if (titles.isEmpty())
         return Optional.empty();

      Optional<Title> title = Optional.empty();
      for (String type : types)
      {
         title = titles.stream()
            .filter(t -> type.equalsIgnoreCase(t.getType()))
            .findAny();

         if (title.isPresent())
            break;
      }

      return title.isPresent() ? title : Optional.of(titles.iterator().next());
   }

   private static int compare(Title a, Title b)
   {
      int score = a.getFullTitle().compareTo(b.getFullTitle());
      return (score != 0)
            ? score : Integer.compare(a.hashCode(), b.hashCode());
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