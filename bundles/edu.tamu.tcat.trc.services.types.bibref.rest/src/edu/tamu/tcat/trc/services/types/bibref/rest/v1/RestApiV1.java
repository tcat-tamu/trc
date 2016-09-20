package edu.tamu.tcat.trc.services.types.bibref.rest.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RestApiV1
{
   public static class ReferenceCollection
   {
      public Map<String, Citation> citations = new HashMap<>();
      public Map<String, BibliographicItem> bibliography = new HashMap<>();
   }

   public static class Citation
   {
      public String id;
      public List<BibliographicItemReference> items = new ArrayList<>();
   }

   public static class BibliographicItemReference
   {
      public String id;
      public String locatorType;
      public String locator;
      public String label;
   }

   public static class BibliographicItem
   {
      public String id;
      public String type;
      public BibliographicItemMeta meta = new BibliographicItemMeta();
      public List<Creator> creators = new ArrayList<>();
      public Map<String, String> fields = new HashMap<>();
   }

   public static class BibliographicItemMeta
   {
      public String key;
      public String creatorSummary;
      public String parsedDate;
      public String dateAdded;
      public String dateModified;
   }

   public static class Creator
   {
      public String role;
      public String firstName;
      public String lastName;
      public String name;
   }
}
