package edu.tamu.tcat.trc.entries.types.article.impl.model;

import edu.tamu.tcat.trc.entries.types.article.Footnote;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.DataModelV1;

public class FootnoteImpl implements Footnote
{
   private final String id;
   private final String backlinkId;
   private final String content;
   private final String mimeType;

   public FootnoteImpl(DataModelV1.Footnote dto)
   {
      this.id = dto.id;
      this.backlinkId = dto.backlinkId;
      this.content = dto.content;
      this.mimeType = dto.mimeType;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String getBacklinkId()
   {
      return backlinkId;
   }

   @Override
   public String getContent()
   {
      return content;
   }

   @Override
   public String getMimeType()
   {
      return mimeType;
   }

}
