package edu.tamu.tcat.trc.services.types.bibref.rest.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BibRefRestApiV1
{
   public static class ReferenceCollection
   {
      public final Map<String, Citation> citations = new HashMap<>();
      public final Map<String, BibliographicItem> bibliography = new HashMap<>();
   }

   public static class Citation
   {
      public String id;
      public final List<BibliographicItemReference> items = new ArrayList<>();
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
      public final BibliographicItemMeta meta = new BibliographicItemMeta();
      public final List<Creator> creators = new ArrayList<>();
      public final Map<String, String> fields = new HashMap<>();
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
