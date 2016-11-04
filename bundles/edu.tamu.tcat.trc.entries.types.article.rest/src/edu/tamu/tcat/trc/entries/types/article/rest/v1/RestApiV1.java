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
package edu.tamu.tcat.trc.entries.types.article.rest.v1;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.tamu.tcat.trc.resolver.EntryIdDto;


public abstract class RestApiV1
{

   /**
    * @since 1.1
    */
   public static class Article
   {
      /**
       * Authoritative link to this version of an article. Note that articles can be
       * obtained from multiple different URLs (for example, if they are part of multiple
       * collections). This link provides a reference to the 'canonical' form of the article.
       */
      public Link self;

      /** The unique identifier for this article. */
      public String id;

      /** The version of this article. Modifications will increment the version of the
       *  article. Reserved for future use. */
      public int version;

      /** A unique token that identifies this article with the entry resolver registry */
      public EntryIdDto ref;

      /** An application defined type for this article. For example, an application may wish
       *  to distinguish between editorials, book reviews, and research papers. */
      public String articleType;

      /** The mime-type of the article's body content. */
      public String contentType;

      /** The title of the document for display. */
      public String title;

      /** The authors or other creators of this article. */
      public List<ArticleAuthor> authors;

      /** A summary of the article for introductory purposes. */
      @JsonProperty("abstract")
      public String articleAbstract;

      /** The main content of the article. */
      public String body;

      /** A map from footnote id to footnote. */
      public Map<String, Footnote> footnotes = new HashMap<>();
   }

   public static class ArticleAuthor
   {
      public String id;
      public String name;
      public String lastname;
      public String firstname;

      public Map<String, String> properties = new HashMap<>();
   }

   public static class Contact
   {
      public String email;
      public String phone;
   }

   public static class Footnote
   {
      public String id;
      public String backlinkId;
      public String content;
      public String mimeType;
   }

   /**
    * @since 1.1
    */
   public static class ArticleSearchResult
   {
      public String id;
      public EntryIdDto ref;
      public String title;
      public List<String> authors;
      public List<String> absHL = new ArrayList<>();
      public List<String> contentHL = new ArrayList<>();
   }

   /**
    * @since 1.1
    */
   public static class ArticleSearchResultSet
   {
      public QueryDetail query;
      public List<ArticleSearchResult> articles = new ArrayList<>();
      // TODO add facets and highlighting
   }

   /**
    * @since 1.1
    */
   public static class QueryDetail
   {
      /**
       * Free-text query string to be matched against the article title, text and other
       * properties. May be {@code null} or empty string in which case all articles will be
       * returned, respecting the result paging limits.
       */
      public String q;

      /**
       * Indicates whether hit highlighting should be enabled.
       */
      public boolean highlight = true;

      /**
       * The index of the first result to return. Used to support result paging.
       */
      public int offset;

      /**
       * The index of the last result to return.
       */
      public int max;

      /**
       * The page sequence number for the current result set.
       */
      public int pg;

      /**
       * The total number of results that matched the supplied query (excluding paging
       * parameters). Note that this may be approximate.
       */
      public int totalResults;

      /**
       * The number of pages that can be returned for this query. This is equivilant to
       * {@code totalResults / max + 1}.
       */
      public int numPages;

      /**
       * A template for use in building paged URL queries. Replace the template parameter {0}
       * with the offset value for the desired page (i.e., {@code (pg - 1) * max} for page
       * indices in the range {@code [1, numPages]}.
       */
      public String pageUrlTemplate;

      /**
       * A {@link Link} to re-execute this query. Note that the results of running a query
       * again will typically be identical but may change if the underlying search indices have
       * been updated.
       */
      public Link self;

      /**
       * A {@link Link} to retrieve the next page of query results. May be {@code null} if there
       * are no more results to be retrieved (i.e., this is the last page of results).
       */
      public Link next;

      /**
       * A {@link Link} to retrieve the preceding page of query results. May be {@code null} if
       * there are no preceding results to retrieve (i.e., this is the first page of results).
       */
      public Link previous;

      /**
       * A {@link Link} to retrieve the first page of query results. Will not be {@code null}.
       * This may be identical to {@code last} if there is only one page of results.
       */
      public Link first;

      /**
       * A {@link Link} to retrieve the last page of query results. Will not be {@code null}.
       * This may be identical to {@code first} if there is only one page of results.
       */
      public Link last;
   }

   /**
    * @since 1.1
    */
   public static class Link
   {
      /**
       * The URI for this link.
       */
      public URI uri;

      /**
       * The relationship between this link and the current resource.
       */
      public String rel;

      /**
       * A title for this link suitable for display.
       */
      public String title;
   }
}
