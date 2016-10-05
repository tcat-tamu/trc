package edu.tamu.tcat.trc.entries.types.article;

public interface Footnote
{
   /**
    * @return A unique identifier for this footnote.
    */
   String getId();

   /**
    * @return A target that links this footnote to some position in the {@link Article} text.
    */
   String getBacklinkId();

   /**
    * @return The content of this footnote.
    */
   String getContent();

   /**
    * @return The MIME type of this footnote's encoded content.
    */
   String getMimeType();
}
