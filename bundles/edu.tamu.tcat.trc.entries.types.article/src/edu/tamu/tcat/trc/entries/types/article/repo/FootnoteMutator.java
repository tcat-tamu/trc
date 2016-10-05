package edu.tamu.tcat.trc.entries.types.article.repo;

public interface FootnoteMutator
{
   /**
    * @return The id of the footnote being edited.
    */
   String getId();

   /**
    * Sets the footnote backlink id.
    * @param backlinkId
    */
   void setBacklinkId(String backlinkId);

   /**
    * Sets the content of this footnote.
    * @param content
    */
   void setContent(String content);

   /**
    * Sets the type of the footnote contents.
    * @param mimeType
    */
   void setMimeType(String mimeType);
}
