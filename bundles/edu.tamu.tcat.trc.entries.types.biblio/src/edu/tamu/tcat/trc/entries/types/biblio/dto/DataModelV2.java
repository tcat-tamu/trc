package edu.tamu.tcat.trc.entries.types.biblio.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class DataModelV2
{
   // NOTE this is (loosely based on the bibliographic description formats used by Zotero
   //      See edu.tamu.tcat.zotero.basic.v3.model for more detail

   public static class BibliographicInfo
   {
      /** The id for this entry on Zotero. NOTE may need to be structured. */
      public String zoteroId;

      /** The bibliographic type of this item (such as book, article). */
      public String bibType;

      public List<AuthorReferenceDTO> creators;

      public Map<String, String> fieldValues;
   }

   public static class BaseBibliographicItem
   {
      public String id;

      /** The title of this bibliographic item. For short format display. */
      public String title;

      /** A simplified format of the creator information for display. For short format display. */
      public String creator;

      /** A simplified representation of the date this item was created. For short format display. */
      public String date;

      public BibliographicInfo details;

      public String summary;

      /** Identifier for the default copy. */
      public String primary = null;

      /** References to digital copies associated with this item. */
      public Set<DigitalCopy> copies = new HashSet<>();
   }

   public static class Work extends BaseBibliographicItem
   {
      public List<Edition> editions = new ArrayList<>();
   }

   public static class Edition extends BaseBibliographicItem
   {
      public String name;

      public List<Volume> editions = new ArrayList<>();
   }

   public static class Volume extends BaseBibliographicItem
   {
      public String number;
   }

   public static class DigitalCopy
   {
      public String id;
      public String type;
      public Map<String, String> properties = new HashMap<>();
      public String title;
      public String summary;
      public String rights;
   }
}
