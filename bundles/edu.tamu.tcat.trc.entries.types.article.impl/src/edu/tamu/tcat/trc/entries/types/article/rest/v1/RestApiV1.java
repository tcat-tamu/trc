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
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class RestApiV1
{

   public static class ArticleId
   {
      public String id;
      /**
       * @since 1.1
       */
      public String uri;
   }

   /**
    * @since 1.1
    */
   public static class Article
   {
      public Link self;
      public String id;
      public String type;
      public String title;
      public Publication pubInfo;
      public List<ArticleAuthor> authors;
      public String articleAbstract;
      public String body;
      public List<FootNote> footnotes;
      public List<Citation> citations;
      public List<Bibliography> bibliography;
      public List<ArticleLink> links;
      public Theme theme;
   }
   
   public static class Publication
   {
      public Date dateCreated;
      public Date dateModified;
   }
   
   public static class ArticleAuthor
   {
      public String id;
      public String name;
      public String affiliation;
      public Contact contact;
   }
   
   public static class Contact
   {
      public String email;
      public String phone;
   }
   
   public static class FootNote
   {
      public String id;
      public String text;
   }
   
   public static class Citation
   {
      
      public String citationID;
      public List<CitationItem> citationItems;
      public ArticleProperties properties;
   }
   
   public static class CitationItem
   {
      public String id;
      public String locator;
      public String label;
      @JsonProperty("suppress-author") public String suppressAuthor;
   }
   
   public static class ArticleProperties
   {
      public String properties;
   }
   
   public static class Bibliography
   {
      public String id;
      public String type;
      public String title;
      public String edition;
      public List<Author> author;
      public List<Translator> translator;
      public String publisher;
      @JsonProperty("container-title") public String containerTitle;
      @JsonProperty("publisher-place") public String publisherPlace;
      public String URL;
      public Issued issued;
   }
   
   
   public static class Author
   {
      public String family;
      public String given;
   }
   
   public static class Translator
   {
      public String family;
      public String given;
      public String literal;
   }
   
   public static class Issued
   {
      @JsonProperty("date-parts") public List<List<String>> dateParts;
   }
   
   public static class ArticleLink
   {
      public String id;
      public String type;
      public String title;
      public String uri;
      public String rel;
   }
   
   public static class Theme
   {
      public String title;
      public String themeAbstract;
      public List<Articles> articles;
      
   }
   
   public static class Articles
   {
      public String id;
      public String type;
      public String uri;
   }
   /**
    * @since 1.1
    */
   public static class ArticleSearchResult
   {
      public String id;
      public String title;
      public Publication pubInfo;
      public List<ArticleAuthor> authors;
      public List<String> absHL;
      public List<String> contentHL;
   }

   /**
    * @since 1.1
    */
   public static class ArticleSearchResultSet
   {
      public QueryDetail query;
      public List<ArticleSearchResult> articles;
      // TODO add facets
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