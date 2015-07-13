package edu.tamu.tcat.trc.entries.types.article;

import java.net.URI;
import java.util.UUID;

public interface Article
{

   /**
    * @return A unique identifier for this article.
    */
   UUID getId();

   /**
    * @return String representation of the Title of the Article.
    */
   String getTitle();

   /**
    * @return URI of the entity to which they are attached. Note that this may be an element
    *    within a catalog entry (e.g., attached to an author reference) once the entry type
    *    itself provides well-defined URIs for those internal components.
    */
   URI getEntity();

   /**
    * @return An application defined unique identifier for the author.
    */
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
