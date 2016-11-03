package edu.tamu.tcat.trc.entries.types.biblio;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

import edu.tamu.tcat.trc.entries.common.DateDescription;

public abstract class BiblioEntryUtils
{
   public static String parseAuthorName(BibliographicEntry entry)
   {
      List<AuthorReference> authors = entry.getAuthors();
      if (authors.isEmpty())
         return null;

      AuthorReference ref = authors.get(0);
      String name = trimToNull(ref.getLastName());
      if (name == null)
         name = trimToNull(ref.getFirstName());

      return name;
   }

   public static String parsePublicationDate(BibliographicEntry entry)
   {
      LocalDate d = entry.getEditions().stream()
            .map(Edition::getPublicationInfo).filter(Objects::nonNull)
            .map(PublicationInfo::getPublicationDate).filter(Objects::nonNull)
            .map(DateDescription::getCalendar).filter(Objects::nonNull)
            .min(LocalDate::compareTo)
            .orElse(null);

      return normalizedYear(d);
   }

   /**
    * Provides a simple title for the supplied work. If no title is defined, this will  This will first attempt to return the
    * short title, if that is not found, will return the canonical title. If neither are
    * found it will return the first title in the list or a message Finally, returns
    * the first title found.
    *
    * @param entry The bibliographic entry whose title should be returned.
    * @param types A prioritized list of titles to attempt to retrieve. For example, an
    *       application may want to retrieve first the 'short' title and then the 'canonical'
    *       title and (if neither are found) any title.
    * @return The first matching title.
    */
   public static Optional<String> parseTitle(BibliographicEntry entry, String... types)
   {
      return getTitle(entry, types).map(Title::getFullTitle);
   }

   /**
    * Obtains a single title for the supplied bibliographical entry. This returned title
    * will be one of the supplied list of types or, if none of the supplied types are
    * available, the first available title. If no titles are defined for this bibliographic
    * entry, the returned optional will be empty.
    *
    * @param entry The bibliographic entry whose title should be returned.
    * @param types A prioritized list of titles to attempt to retrieve. For example, an
    *       application may want to retrieve first the 'short' title and then the 'canonical'
    *       title and (if neither are found) any title.
    * @return The first matching title.
    */
   public static Optional<Title> getTitle(BibliographicEntry entry, String... types)
   {
      TreeSet<Title> titles = new TreeSet<>(BiblioEntryUtils::compare);
      titles.addAll(entry.getTitle().get());

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

   /** @return the year this work was published. May be null */
   private static String normalizedYear(LocalDate d)
   {
      if (d == null)
         return null;

      // correct for year-dates that were offset due to UTC conversion to Dec 31st.
      // HACK: should be pushed closer to data source and/or removed once data is updated
      int yr = d.getYear();
      int mo = d.getMonthValue();
      int day = d.getDayOfMonth();
      if (mo == 12 && day == 31)
      {
         yr++;             // assume that what was meant as just a year
      }

      return String.valueOf(yr);
   }

   private static String trimToNull(String value)
   {
      return (value == null || value.trim().isEmpty()) ? null : value.trim();
   }
}
