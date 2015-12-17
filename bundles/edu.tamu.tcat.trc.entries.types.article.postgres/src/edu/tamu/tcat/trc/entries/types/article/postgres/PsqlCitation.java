package edu.tamu.tcat.trc.entries.types.article.postgres;

import edu.tamu.tcat.trc.entries.types.article.Citation;
import edu.tamu.tcat.trc.entries.types.article.dto.CitationDTO.CitationPropertiesDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.CitationItemDTO;

public class PsqlCitation implements Citation
{

   private String id;
   private CitationProperties properties;
   private String suppressAuthor;
   private CitationItem citationItems;

   public PsqlCitation(String id, CitationPropertiesDTO properties, String suppressAuthor, CitationItemDTO citationItems)
   {
      this.id = id;
      this.properties = new PsqlCitationProperties();
      this.suppressAuthor = suppressAuthor;
      this.citationItems = new PsqlCitationItem(citationItems.id, citationItems.label, citationItems.locator);
   }

   @Override
   public String getId()
   {
      return this.id;
   }

   @Override
   public CitationItem getItems()
   {
      return this.citationItems;
   }

   @Override
   public CitationProperties getProperties()
   {
      return this.properties;
   }

   @Override
   public String getSupressAuthor()
   {
      return this.suppressAuthor;
   }
   
   public static class PsqlCitationItem implements CitationItem
   {

      private String id;
      private String label;
      private String locator;

      public PsqlCitationItem(String id, String label, String locator)
      {
         this.id = id;
         this.label = label;
         this.locator = locator;
      }

      @Override
      public String getId()
      {
         return this.id;
      }

      @Override
      public String getLocator()
      {
         return this.label;
      }

      @Override
      public String getLabel()
      {
         return this.locator;
      }
   }
   
   public static class PsqlCitationProperties implements CitationProperties
   {
      
   }

}
