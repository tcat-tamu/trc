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
package edu.tamu.tcat.trc.entries.types.biblio.search;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.biblio.AuthorList;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.TitleDefinition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;


/**
 * JSON serializable summary information about a work. Intended to be
 * returned when only a brief summary of the work is required to save
 * data transfer and parsing resources.
 *
 */
public class BiblioSearchProxy
{
   // FIXME this mixes works, editions and volume information

   public String id;
   @Deprecated // see note on Work#getType()
   public String type;
   public String uri;
   public List<AuthorReferenceDTO> authors = new ArrayList<>();
   public String title;
   public String label;
   public String summary;
   public String pubYear = null;

   public static BiblioSearchProxy create(Work w)
   {
      BiblioSearchProxy result = new BiblioSearchProxy();

      TitleDefinition titleDefn = w.getTitle();
      Set<Title> titles = titleDefn.get();
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
      result.type = w.getType();
      result.uri = "works/" + w.getId();        // TODO make a more flexible tool for creating work URIs
      result.title = getEntityTitle(titles);

      result.label = constructLabel(titles, name, pubYear);
      result.pubYear = pubYear;

      result.summary = w.getSummary();

      authors.forEach(author -> result.authors.add(AuthorReferenceDTO.create(author)));

      return result;
   }

   public static BiblioSearchProxy create(String workId, Edition e)
   {
      BiblioSearchProxy result = new BiblioSearchProxy();

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
      authors.forEach(author -> result.authors.add(AuthorReferenceDTO.create(author)));

      return result;

   }

   public static BiblioSearchProxy create(String workId, String editionId, Volume v)
   {
      BiblioSearchProxy result = new BiblioSearchProxy();
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

      authors.forEach(author -> result.authors.add(AuthorReferenceDTO.create(author)));

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
         {
            name = trimToNull(ref.getFirstName());
         }
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

   public BiblioSearchProxy()
   {
   }

}
