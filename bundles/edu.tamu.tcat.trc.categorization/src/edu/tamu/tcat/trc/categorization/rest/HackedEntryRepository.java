package edu.tamu.tcat.trc.categorization.rest;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HackedEntryRepository
{
   public final Map<String, RestApiV1.Categorization> categorizations = new HashMap<>();
   // temporary for in-memory use only
   // used for initial dev and test of REST API.
   // to be replaced once CategorizationRepository API has been defined.

   RestApiV1.Categorization createHierarchicalCategorization(RestApiV1.Categorization categorization)
   {
      categorization.meta = initMetadata();
      categorization.entries = createRootEntry(categorization);

      RestApiV1.Categorization result = new RestApiV1.Categorization();
      result.key = categorization.key;
      result.label = categorization.label;
      result.description = categorization.description;
      result.type = categorization.type;

      // TODO copy entries

      categorizations.put(result.key, result);

      return result;
   }

   private RestApiV1.HierarchyEntry createRootEntry(RestApiV1.Categorization categorization)
   {
      RestApiV1.HierarchyEntry root = new RestApiV1.HierarchyEntry();
      root.articleReference = null;
      root.categorizationId = categorization.meta.id;
      root.version = categorization.meta.version;

      root.id = UUID.randomUUID().toString();
      root.label = categorization.label;
      root.slug = null;
      root.parentId = null;
      root.childIds = new ArrayList<>();
      root.children = new ArrayList<>();
      return root;
   }

   private RestApiV1.CategorizationMeta initMetadata()
   {
      RestApiV1.CategorizationMeta meta = new RestApiV1.CategorizationMeta();
      meta.id = UUID.randomUUID().toString();
      meta.version = 1;

      String creationTime = DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now());
      meta.dateCreated = creationTime;
      meta.dateModified = creationTime;

      return meta;
   }


}
