package edu.tamu.tcat.trc.entries.types.biblio.repo.copies;

import java.util.Map;

public interface CopyReferenceMutator
{
   /**
    * @return the ID of the copy reference being edited.
    */
   String getId();

   /**
    * @param type An identifier for the origin or type of the referenced digital copy.
    */
   void setType(String type);

   /**
    * @param id The information necessary to resolve this reference to the associated digital copy.
    *       For example, the remote service's identifier, sequence/page number, a URI, etc.
    */
   void setProperties(Map<String, String> properties);

   /**
    * @param title A title that describes this relationship between the work and the digital copy.
    *       Examples would include 'Black and White', 'High Resolution Color Scan',
    *       'Harvard Copy'. May be an empty string. {@code null} values will be converted to the
    *       empty string.
    */
   void setTitle(String title);

   /**
    * @param summary A short description (if desired) that describes interesting features of the
    *       linked copy to aid users in understanding its relevance to their reading. For
    *       example, this might be used to note missing pages, significant annotations or
    *       provenance, or the accuracy of OCR.  May be an empty string. {@code null} values
    *       will be converted to the empty string.
    */
   void setSummary(String summary);

   /**
    * @param description A description of the usage rights of this work.  May be an empty
    *       string. {@code null} values will be converted to the empty string.
    */
   void setRights(String description);     // TODO use structured model for rights.
}
