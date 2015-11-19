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

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import edu.tamu.tcat.trc.entries.types.article.dto.ArticleAuthorDTO;

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
   UUID getId();

   /**
    * @return String representation of the title of the article.
    */
   String getTitle();
   
   /**
    * 
    * @return List of Authors that have contributed to the article.
    */
   List<ArticleAuthor> getAuthors();
   
   String getSlug();
   
   /**
    * 
    * @return String representation of the summary of the article.
    */
   String getAbstract();
   
   /**
    * 
    * @return Original publication date of the article.
    */
   Date getPublishedDate();
   
   /**
    * 
    * @return Last modification date of the article. May return null if the article has not been updated.
    */
   Date getLastModified();
   
   /**
    * This is API is provisional. It is under review to determine if this use case would be
    * better met through a different mechanism.
    *
    * @return URI of an entity to which this article is associated. For example, if this article
    *    is a book review, this would be the URI of the corresponding bibliographic entry.
    * @deprecated This needs to be extended to support association with multiple entities.
    *       This may be implemented using the relationships API.
    */
   @Deprecated
   URI getEntity();

   /**
    * @return An application defined unique identifier for the author.
    * @deprecated Needs to be extended to support a more full-featured authorship attribution
    *       model.
    */
   @Deprecated
   UUID getAuthorId();

   /**
    * @return The type of content for the article. Currently we anticipate only
    *    text or HTML articles but applications may provide support for other data types
    *    (e.g. Markdown, XML, SVG, etc) provided that they can be represented as contents
    *    of a JSON document.
    */
   String getMimeType();

   /**
    * @return The content of the article.
    */
   String getContent();
}
