package edu.tamu.tcat.trc.entries.types.article.dto;

import edu.tamu.tcat.trc.entries.types.article.Footnote;

public class FootnoteDTO
{
   public String id;
   public String text;
   
   public static FootnoteDTO create(Footnote f)
   {
      FootnoteDTO dto = new FootnoteDTO();
      dto.id = f.getId();
      dto.text = f.getText();
      return dto;
   }
}
