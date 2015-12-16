package edu.tamu.tcat.trc.entries.types.article.dto;

import java.util.List;

public class BibliographyDTO
{
   public String id;
   public String type;
   public String title;
   public BibAuthorDTO author;
   public BibTranslatorDTO translator;
   public String publisher;
   public String publisherPlace;
   public String url;
   public List<IssuedBiblioDTO> issued;
   
   
   public class BibAuthorDTO
   {
      public String family;
      public String given;
   }
   
   public class BibTranslatorDTO
   {
      public String family;
      public String given;
   }
   
   public class IssuedBiblioDTO
   {
      public String dateParts;
   }
}
