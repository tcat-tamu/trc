package edu.tamu.tcat.trc.notes;

import java.net.URI;
import java.util.UUID;

public interface Notes
{
   public enum NotesMimeType
   {
      TEXT,
      HTML
   }

   UUID getId();

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
    * @return The type of content for the note. Currently we anticipate only
    *    text or HTML notes but applications may provide support for other data types
    *    (e.g. Markdown, XML, SVG, etc) provided that they can be represented as contents
    *    of a JSON document.
    */
   String getMimeType();

   /**
    * @return The content of the note.
    */
   String getContent();
}
