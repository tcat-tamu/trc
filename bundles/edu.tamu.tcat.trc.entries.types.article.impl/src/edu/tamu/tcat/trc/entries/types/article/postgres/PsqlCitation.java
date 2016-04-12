package edu.tamu.tcat.trc.entries.types.article.postgres;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.article.Citation;
import edu.tamu.tcat.trc.entries.types.article.dto.CitationItemDTO;

public class PsqlCitation implements Citation
{

   private String id;
   private List<CitationItem> citationItems;

   public PsqlCitation(String id, List<CitationItemDTO> citationItems)
   {
      this.id = id;
      this.citationItems = new ArrayList<>();
      
      citationItems.forEach((ci) -> {
         this.citationItems.add(new PsqlCitationItem(ci.id, ci.label, ci.locator, ci.suppressAuthor));
      });
   }

   @Override
   public String getId()
   {
      return this.id;
   }

   @Override
   public List<CitationItem> getItems()
   {
      return this.citationItems;
   }
   
   public static class PsqlCitationItem implements CitationItem
   {

      private String id;
      private String label;
      private String locator;
      private String suppressAuthor;

      public PsqlCitationItem(String id, String label, String locator, String suppressAuthor)
      {
         this.id = id;
         this.label = label;
         this.locator = locator;
         this.suppressAuthor = suppressAuthor;
      }

      @Override
      public String getId()
      {
         return this.id;
      }

      @Override
      public String getLocator()
      {
         return this.locator;
      }

      @Override
      public String getLabel()
      {
         return this.label;
      }

      @Override
      public String getSuppressAuthor()
      {
         return suppressAuthor;
      }
   }
}
