package edu.tamu.tcat.trc.services.types.bibref.impl.repo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class DataModelV1
{
   public static class Bibliography
   {
      public String id;
      public final Map<String, Citation> citations = new HashMap<>();
      public final Map<String, BibliographicItem> items = new HashMap<>();

      /**
       * @param original The instance to copy.
       * @return A new copy of the given bibliography.
       */
      public static Bibliography copy(Bibliography original)
      {
         return copy(new Bibliography(), original);
      }

      /**
       * @param dest The destination instance that will be updated in-place with values from the source.
       * @param source The instance from which to copy values
       * @return The destination containing all copied values.
       */
      public static Bibliography copy(Bibliography dest, Bibliography source)
      {
         Objects.requireNonNull(dest, "destination must not be null");
         Objects.requireNonNull(source, "source must not be null");

         dest.id = source.id;

         dest.citations.clear();
         source.citations.values().stream()
               .map(Citation::copy)
               .forEach(citation -> dest.citations.put(citation.id, citation));

         dest.items.clear();
         source.items.values().stream()
               .map(BibliographicItem::copy)
               .forEach(item -> dest.items.put(item.id, item));

         return dest;
      }
   }

   public static class Citation
   {
      public String id;
      public final List<BibliographicItemReference> citedItems = new ArrayList<>();

      /**
       * @param original The instance to copy.
       * @return A new copy of the given citation.
       */
      public static Citation copy(Citation original)
      {
         return copy(new Citation(), original);
      }

      /**
       * @param dest The destination instance that will be updated in-place with values from the source.
       * @param source The instance from which to copy values.
       * @return The destination containing all copied values.
       */
      public static Citation copy(Citation dest, Citation source)
      {
         Objects.requireNonNull(dest, "destination must not be null");
         Objects.requireNonNull(source, "source must not be null");

         dest.id = source.id;

         dest.citedItems.clear();
         source.citedItems.stream()
               .map(BibliographicItemReference::copy)
               .forEach(dest.citedItems::add);

         return dest;
      }
   }

   public static class BibliographicItemReference
   {
      public String itemId;
      public String locatorType;
      public String locator;
      public String label;

      /**
       * @param original The instance to copy.
       * @return A new copy of the given bibliographic item reference.
       */
      public static BibliographicItemReference copy(BibliographicItemReference original)
      {
         return copy(new BibliographicItemReference(), original);
      }

      /**
       * @param dest The destination instance that will be updated in-place with values from the source.
       * @param source The instance from which to copy values.
       * @return The destination containing all copied values.
       */
      public static BibliographicItemReference copy(BibliographicItemReference dest, BibliographicItemReference source)
      {
         Objects.requireNonNull(dest, "destination must not be null");
         Objects.requireNonNull(source, "source must not be null");

         dest.itemId = source.itemId;
         dest.locatorType = source.locatorType;
         dest.locator = source.locator;
         dest.label = source.label;

         return dest;
      }
   }

   public static class BibliographicItem
   {
      public String id;
      public String type;
      public final BibliographicItemMeta meta = new BibliographicItemMeta();
      public final List<Creator> creators = new ArrayList<>();
      public final Map<String, String> fields = new HashMap<>();

      /**
       * @param original The instance to copy.
       * @return A new copy of the given bibliographic item.
       */
      public static BibliographicItem copy(BibliographicItem original)
      {
         return copy(new BibliographicItem(), original);
      }

      /**
       * @param dest The destination instance that will be updated in-place with values from the source.
       * @param source The instance from which to copy values.
       * @return The destination containing all copied values.
       */
      public static BibliographicItem copy(BibliographicItem dest, BibliographicItem source)
      {
         Objects.requireNonNull(dest, "destination must not be null");
         Objects.requireNonNull(source, "source must not be null");

         dest.id = source.id;
         dest.type = source.type;

         BibliographicItemMeta.copy(dest.meta, source.meta);

         dest.creators.clear();
         source.creators.stream()
               .map(Creator::copy)
               .forEach(dest.creators::add);

         dest.fields.clear();
         dest.fields.putAll(source.fields);

         return dest;
      }
   }

   public static class BibliographicItemMeta
   {
      public String key;
      public String creatorSummary;
      public String parsedDate;
      public String dateAdded;
      public String dateModified;

      /**
       * @param original The instance to copy.
       * @return A new copy of the given bibliographic item metadata.
       */
      public static BibliographicItemMeta copy(BibliographicItemMeta original)
      {
         return copy(new BibliographicItemMeta(), original);
      }

      /**
       * @param dest The destination instance that will be updated in-place with values from the source.
       * @param source The instance from which to copy values.
       * @return The destination containing all copied values.
       */
      public static BibliographicItemMeta copy(BibliographicItemMeta dest, BibliographicItemMeta source)
      {
         Objects.requireNonNull(dest, "destination must not be null");
         Objects.requireNonNull(source, "source must not be null");

         dest.key = source.key;
         dest.creatorSummary = source.creatorSummary;
         dest.parsedDate = source.parsedDate;
         dest.dateAdded = source.dateAdded;
         dest.dateModified = source.dateModified;

         return dest;
      }
   }

   public static class Creator
   {
      public String role;
      public String firstName;
      public String lastName;
      public String name;

      /**
       * @param original The instance to copy.
       * @return A new copy of the given creator.
       */
      public static Creator copy(Creator original)
      {
         return copy(new Creator(), original);
      }

      /**
       * @param dest The destination instance that will be updated in-place with values from the source.
       * @param source The instance from which to copy values.
       * @return The destination containing all copied values.
       */
      public static Creator copy(Creator dest, Creator source)
      {
         Objects.requireNonNull(dest, "destination must not be null");
         Objects.requireNonNull(source, "source must not be null");

         dest.role = source.role;
         dest.firstName = source.firstName;
         dest.lastName = source.lastName;
         dest.name = source.name;

         return dest;
      }
   }
}
