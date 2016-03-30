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
package edu.tamu.tcat.trc.entries.types.bib.rest.v1;

import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An encapsulation of the data vehicle types used to process JSON requests and responses
 * for version 1 of the TRC REST API for Bibliographic entries.
 */
public class RestApiV1
{
   public static class WorkSearchResultSet
   {
      public List<WorkSearchResult> items;
      /** The querystring that resulted in this result set */
      public String qs;
      public String qsNext;
      public String qsPrev;
   }

   public static class WorkSearchResult
   {
      // work id; used in URIs
      public String id;
      public String type;
      // relative uri to the work, e.g. /work/{id}
      public String uri;
      public List<AuthorRef> authors;
      public String title;
      public String label;
      public String summary;
      public String pubYear;
   }

   public static class AuthorRef
   {
      public String authorId;
      public String name;
      public String firstName;
      public String lastName;
      public String role;
   }

   public static class WorkId
   {
      public String id;
   }

   public static class EditionId
   {
      public String id;
   }

   public static class VolumeId
   {
      public String id;
   }

   public static class Work
   {
      public String id;
      public String type;
      public List<AuthorRef> authors;
      public Collection<Title> titles;
      public List<AuthorRef> otherAuthors;
      public String series;
      public String summary;

      public Collection<Edition> editions;
   }

   public static class Title
   {
      // short, default, undefined.
      public String type;
      // language
      public String lg;
      public String title;
      public String subtitle;
   }

   public static class Edition
   {
      public String id;
      public String editionName;
      public PublicationInfo publicationInfo;
      public List<AuthorRef> authors;
      public Collection<Title> titles;
      public List<AuthorRef> otherAuthors;
      public String summary;
      public String series;
      public List<Volume> volumes;
   }

   public static class Volume
   {
      public String id;
      public String volumeNumber;
      public PublicationInfo publicationInfo;
      public List<AuthorRef> authors;
      public Collection<Title> titles;
      public List<AuthorRef> otherAuthors;
      public String summary;
      public String series;
   }

   public static class PublicationInfo
   {
      public String publisher;
      public String place;
      public DateDescription date;
   }

   public static class DateDescription
   {
      /** ISO 8601 local (YYYY-MM-DD) representation of this date. */
      public String calendar;

      /** A human readable description of this date. */
      public String description;     // NOTE use this to capture intended degree of precision
   }

   /**
    * A DTO to be used as a REST query or path parameter. This class parses the String
    * sent (using the REST API format) as the parameter value into a date range.
    * <p>
    * The value of a single string for a compound parameter is that the atomic values may be used
    * as unique facet items and collected in lists rather than parsed and matched as multiple
    * values in query or path parameters.
    */
   public static class DateRangeParam
   {
      private static String REGEX = "(\\d*|\\*)\\.\\.(\\d*|\\*)";
      private static Pattern PATTERN = Pattern.compile(REGEX);

      public final Year start;
      public final Year end;

      public DateRangeParam(String p) throws Exception
      {
         Matcher m = PATTERN.matcher(p);
         if (!m.matches())
            throw new IllegalArgumentException("Date range ["+p+"] does not follow expected format");

         start = Year.parse(m.group(1));
         end = Year.parse(m.group(2));
      }

      /**
       * Convert this {@link DateRangeParam} back to a parseable value.
       */
      public String toValue()
      {
         return start+".."+end;
      }
   }
}
