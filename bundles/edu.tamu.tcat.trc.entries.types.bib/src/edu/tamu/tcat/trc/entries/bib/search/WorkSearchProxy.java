package edu.tamu.tcat.trc.entries.bib.search;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.tamu.tcat.trc.entries.bib.AuthorList;
import edu.tamu.tcat.trc.entries.bib.AuthorReference;
import edu.tamu.tcat.trc.entries.bib.Edition;
import edu.tamu.tcat.trc.entries.bib.Title;
import edu.tamu.tcat.trc.entries.bib.TitleDefinition;
import edu.tamu.tcat.trc.entries.bib.Volume;
import edu.tamu.tcat.trc.entries.bib.Work;
import edu.tamu.tcat.trc.entries.bib.dto.AuthorRefDV;


/**
 * JSON serializable summary information about a work. Intended to be
 * returned when only a brief summary of the work is required to save
 * data transfer and parsing resources.
 *
 */
public class WorkSearchProxy
{
   // FIXME this mixes works, editions and volume information

   public String id;
   public String uri;
   public List<AuthorRefDV> authors = new ArrayList<>();
   public String title;
   public String label;
   public String summary;
   public String pubYear = null;

   public static WorkSearchProxy create(Work w)
   {
      WorkSearchProxy result = new WorkSearchProxy();

      TitleDefinition titleDefn = w.getTitle();
      Set<Title> titles = titleDefn.getAlternateTitles();
      LocalDate d = w.getEditions().stream()
            .map(ed ->
            ed.getPublicationInfo().getPublicationDate().getCalendar())
            .filter(pubDate ->
            pubDate != null)
            .min(LocalDate::compareTo)
            .orElse(null);

      String pubYear = getNormalizedYear(d);
      AuthorList authors = w.getAuthors();
      List<AuthorReference> authRef = new ArrayList<>();
      authors.forEach(author -> authRef.add(author));

      String name = getAuthorName(authRef);

      result.id = w.getId();
      result.uri = "works/" + w.getId();        // TODO make a more flexible tool for creating work URIs
      result.title = getEntityTitle(titles);

      result.label = constructLabel(titles, name, pubYear);
      result.pubYear = pubYear;

      result.summary = w.getSummary();

      authors.forEach(author -> result.authors.add(AuthorRefDV.create(author)));

      return result;
   }

   public static WorkSearchProxy create(String workId, Edition e)
   {
      WorkSearchProxy result = new WorkSearchProxy();

      Set<Title> titleSet = new HashSet<>(e.getTitles());
      LocalDate d = e.getVolumes().stream()
            .map(ed ->
            ed.getPublicationInfo().getPublicationDate().getCalendar())
            .filter(pubDate ->
            pubDate != null)
            .min(LocalDate::compareTo)
            .orElse(null);

      String pubYear = getNormalizedYear(d);
      String name = getAuthorName(e.getAuthors());

      result.id = e.getId();
      result.uri = "works/" + workId + "/editions/" + e.getId();
      result.title = getEntityTitle(titleSet);

      result.label = constructLabel(titleSet, name, pubYear);
      result.pubYear = pubYear;

      result.summary = e.getSummary();

      List<AuthorReference> authors = e.getAuthors();
      authors.forEach(author -> result.authors.add(AuthorRefDV.create(author)));

      return result;

   }

   public static WorkSearchProxy create(String workId, String editionId, Volume v)
   {
      WorkSearchProxy result = new WorkSearchProxy();
      Set<Title> titleSet = new HashSet<>(v.getTitles());
      LocalDate localDate = v.getPublicationInfo().getPublicationDate().getCalendar();
      String pubYear = getNormalizedYear(localDate);
      List<AuthorReference> authors = v.getAuthors();
      String name = getAuthorName(authors);


      result.id = v.getId();
      result.uri = "works/" + workId + "/editions/" + editionId + "/volumes/" + v.getId();
      result.title = getEntityTitle(titleSet);
      result.label = constructLabel(titleSet, name, pubYear);
      result.pubYear = pubYear;
      result.summary = v.getSummary();

      authors.forEach(author -> result.authors.add(AuthorRefDV.create(author)));

      return result;

   }

   private static String constructLabel(Set<Title> titles, String name, String pubDate)
   {
      StringBuilder sb = new StringBuilder();
      if (name != null)
         sb.append(name).append(pubDate == null ? ", " : " ");

      if (pubDate != null)
         sb.append("(").append(pubDate).append("): ");

      sb.append(getEntityTitle(titles));
      return sb.toString();

   }

   /** @return the author's last name (or best approximate) */
   private static String getAuthorName(List<AuthorReference> authors)
   {
      String name = null;
      if (authors.size() > 0)
      {
         AuthorReference ref = authors.get(0);
         name = trimToNull(ref.getLastName());
         if (name == null)
            name = trimToNull(ref.getFirstName());

         if (name == null)
            name = trimToNull(ref.getName());
      }
      return name;
   }

   private static String getEntityTitle(Set<Title> titles)
   {
      String result = "no title available";
      if (!titles.isEmpty())
      {
         Title title = titles.parallelStream()
                                  .filter(t -> t.getType().equalsIgnoreCase("short"))
                                  .findAny()
                                  .orElse(null);

         if (title == null)
            title = titles.parallelStream()
                              .filter(t -> t.getType().equalsIgnoreCase("canonical"))
                              .findAny()
                              .orElse(titles.iterator().next());

         result = title.getFullTitle();
      }

      return result;
   }

   /** @return the year this work was published. May be null */
   private static String getNormalizedYear(LocalDate d)
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

   public WorkSearchProxy()
   {
   }

}
