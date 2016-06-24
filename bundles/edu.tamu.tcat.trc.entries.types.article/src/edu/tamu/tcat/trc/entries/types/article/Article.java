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
package edu.tamu.tcat.trc.entries.types.article;

import java.util.List;

/**
 *  A long-form article that provides discursive treatment of a topic within a thematic
 *  research collection. The long-form article is a mainstay of scholarly communication, that
 *  allows for an extended narrative treatment of a particular subject.
 *
 *  <p>
 *  The content of an article will typically be provided as lightly marked semantic HTML.
 *  Alternative representations include XML, TEI, Markdown, Text.
 *
 *  <p>
 *  This API is currently provisional. It will be extended and revised significantly prior to
 *  its 1.0 release.
 */
public interface Article
{
   // TODO support multiple authors, possibly with different roles in contributing to the work
   // TODO consider support for multiple content formats (microsummary, overview, article). May implement separately
   // TODO consider support for translations
   // TODO support attachments and/or alternate representations (PDF)

   /**
    * @return A unique identifier for this article.
    */
   String getId();

//   Currently we anticipate only text or HTML articles but applications may provide
//   support for other data types (e.g. Markdown, XML, SVG, etc) provided that they
//   can be represented as contents of a JSON document.

   /**
    * @return The MIME type of content for the article.
    */
   String getContentType();

   /**
    * @return An application-defined semantic type for this article.
    */
   String getArticleType();

   /**
    * @return String representation of the title of the article.
    */
   String getTitle();

   ArticlePublication getPublicationInfo();

   /**
    *
    * @return List of Authors that have contributed to the article.
    */
   List<ArticleAuthor> getAuthors();

   /**
    *
    * @return String representation of the summary of the article.
    */
   String getAbstract();

   /**
    *
    * @return The main ideas and thoughts that represent the article.
    */
   String getBody();

   /**
    *
    * @return A list of footnotes from the article.
    */
   List<Footnote> getFootnotes();

   /**
    *
    * @return A list of citations used to format the bibliographies
    */
   List<Citation> getCitations();

   /**
    *
    * @return A list of bibliographies referenced throughout the article.
    */
   List<Bibliography> getBibliographies();

   /**
    *
    * @return Links that are related to the article.
    */
   List<ArticleLink> getLinks();

   /**
    *  Has not been implemented.
    * @return The representation of the title
    */
   String getSlug();
}
