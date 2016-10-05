package edu.tamu.tcat.trc.impl.psql.services.bibref.repo;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.services.bibref.repo.BibliographicItemMetaMutator;
import edu.tamu.tcat.trc.services.bibref.repo.BibliographicItemMutator;
import edu.tamu.tcat.trc.services.bibref.repo.BibliographicItemReferenceMutator;
import edu.tamu.tcat.trc.services.bibref.repo.CitationMutator;
import edu.tamu.tcat.trc.services.bibref.repo.EditBibliographyCommand;

public class EditBibliographyCommandFactory implements EditCommandFactory<DataModelV1.ReferenceCollection, EditBibliographyCommand>
{
   private final EntryResolverRegistry entryResolverRegistry;

   public EditBibliographyCommandFactory(EntryResolverRegistry entryResolverRegistry)
   {
      this.entryResolverRegistry = entryResolverRegistry;
   }

   @Override
   public EditBibliographyCommand create(String id, UpdateStrategy<DataModelV1.ReferenceCollection> strategy)
   {
      return new EditBibliographyCommandImpl(id, strategy);
   }

   @Override
   public EditBibliographyCommand edit(String id, UpdateStrategy<DataModelV1.ReferenceCollection> strategy)
   {
      return new EditBibliographyCommandImpl(id, strategy);
   }

   public class EditBibliographyCommandImpl implements EditBibliographyCommand
   {
      private final String bibId;
      private final UpdateStrategy<DataModelV1.ReferenceCollection> strategy;
      private final ApplicableChangeSet<DataModelV1.ReferenceCollection> changes = new BasicChangeSet<>();

      public EditBibliographyCommandImpl(String bibId, UpdateStrategy<DataModelV1.ReferenceCollection> strategy)
      {
         this.bibId = bibId;
         this.strategy = strategy;
      }

      @Override
      public EntryReference getEntryReference()
      {
         return entryResolverRegistry.decodeToken(bibId);
      }

      @Override
      public CitationMutator addCitation(String citationId)
      {
         Objects.requireNonNull(citationId, "citation id must not be null");

         changes.add(MessageFormat.format("citations.{0} [create]", citationId), bib -> {
            if (bib.citations.containsKey(citationId))
               throw new IllegalArgumentException(MessageFormat.format("citation id {0} is not unique", citationId));

            DataModelV1.Citation citation = new DataModelV1.Citation();
            citation.id = citationId;
            bib.citations.put(citationId, citation);
         });

         ChangeSet<DataModelV1.Citation> partial = changes.partial(MessageFormat.format("citations.{0}", citationId), makeCitationSelector(citationId));
         return new CitationMutatorImpl(citationId, partial);
      }

      @Override
      public CitationMutator editCitation(String citationId)
      {
         Objects.requireNonNull(citationId, "citation id must not be null");

         ChangeSet<DataModelV1.Citation> partial = changes.partial(MessageFormat.format("citations.{0}", citationId), makeCitationSelector(citationId));
         return new CitationMutatorImpl(citationId, partial);
      }

      @Override
      public void removeCitation(String citationId)
      {
         Objects.requireNonNull(citationId, "citation id must not be null");

         changes.add(MessageFormat.format("citations.{0} [remove]", citationId), bib -> bib.citations.remove(citationId));
      }

      @Override
      public void removeAllCitations()
      {
         changes.add("citations [clear]", bib -> bib.citations.clear());
      }

      @Override
      public BibliographicItemMutator addItem(String itemId)
      {
         Objects.requireNonNull(itemId, "item id must not be null");

         changes.add(MessageFormat.format("items.{0} [create]", itemId), bib -> {
            if (bib.items.containsKey(itemId))
               throw new IllegalArgumentException(MessageFormat.format("item id {0} is not unique", itemId));

            DataModelV1.BibliographicItem item = new DataModelV1.BibliographicItem();
            item.id = itemId;
            bib.items.put(itemId, item);
         });

         ChangeSet<DataModelV1.BibliographicItem> partial = changes.partial(MessageFormat.format("items.{0}", itemId), makeItemSelector(itemId));
         return new BibliographicItemMutatorImpl(itemId, partial);
      }

      @Override
      public BibliographicItemMutator editItem(String itemId)
      {
         Objects.requireNonNull(itemId, "item id must not be null");

         ChangeSet<DataModelV1.BibliographicItem> partial = changes.partial(MessageFormat.format("items.{0}", itemId), makeItemSelector(itemId));
         return new BibliographicItemMutatorImpl(itemId, partial);
      }

      @Override
      public void removeItem(String itemId)
      {
         changes.add(MessageFormat.format("items.{0} [remove]", itemId), bib -> bib.items.remove(itemId));
      }

      @Override
      public void removeAllItems()
      {
         changes.add("items [clear]", bib -> bib.items.clear());
      }

      @Override
      public CompletableFuture<String> execute()
      {
         CompletableFuture<DataModelV1.ReferenceCollection> modified = strategy.update(updateContext -> {
            DataModelV1.ReferenceCollection original = updateContext.getOriginal();
            DataModelV1.ReferenceCollection clone = new DataModelV1.ReferenceCollection();

            if (original == null)
            {
               // we are creating a new bibliography
               clone.id = bibId;
            }
            else
            {
               // we are editing an existing bibliography
               DataModelV1.ReferenceCollection.copy(clone, original);
            }

            return this.changes.apply(clone);
         });

         return modified.thenApply(bib -> bib.id);
      }

      /**
       * @param citationId The id of the citation to select
       * @return A function that, when given a bibliography instance, returns the citation identified by the given {@code citationId} or throws an {@link IllegalArgumentException} if no such citation exists on the bibliography.
       */
      private Function<DataModelV1.ReferenceCollection, DataModelV1.Citation> makeCitationSelector(String citationId)
      {
         return bib -> {
            DataModelV1.Citation citation = bib.citations.get(citationId);
            if (citation == null)
               throw new IllegalArgumentException(MessageFormat.format("bibliography {0} does not contain citation {1}", bibId, citationId));

            return citation;
         };
      }

      /**
       * @param itemId The id of the item to select
       * @return A function that, when given a bibliography instance, returns the bibliographic item identified by the given {@code itemId} or throws an {@link IllegalArgumentException} if no such item exists on the bibliography.
       */
      private Function<DataModelV1.ReferenceCollection, DataModelV1.BibliographicItem> makeItemSelector(String itemId)
      {
         return bib -> {
            DataModelV1.BibliographicItem item = bib.items.get(itemId);
            if (item == null)
               throw new IllegalArgumentException(MessageFormat.format("bibliography {0} does not contain item {1}", bibId, itemId));

            return item;
         };
      }
   }

   private static class CitationMutatorImpl implements CitationMutator
   {
      private static final Logger logger = Logger.getLogger(EditBibliographyCommandFactory.CitationMutatorImpl.class.getName());

      private final String citationId;
      private final ChangeSet<DataModelV1.Citation> changes;

      public CitationMutatorImpl(String citationId, ChangeSet<DataModelV1.Citation> changes)
      {
         this.citationId = citationId;
         this.changes = changes;
      }

      @Override
      public String getId()
      {
         return citationId;
      }

      @Override
      public BibliographicItemReferenceMutator addItemRef(String itemId)
      {
         Objects.requireNonNull(itemId, "item id must not be null.");

         changes.add(MessageFormat.format("citedItems.{0} [create]", itemId), citation -> {
            if (citation.citedItems.stream().anyMatch(ref -> ref.itemId == itemId))
               throw new IllegalArgumentException(MessageFormat.format("item id {0} is not unique on citation {1}", itemId, citationId));

            DataModelV1.BibliographicItemReference ref = new DataModelV1.BibliographicItemReference();
            ref.itemId = itemId;
            citation.citedItems.add(ref);
         });

         ChangeSet<DataModelV1.BibliographicItemReference> partial = changes.partial(MessageFormat.format("citedItems.{0}", itemId), makeItemRefSelector(itemId));
         return new BibliographicItemReferenceMutatorImpl(itemId, partial);
      }

      @Override
      public BibliographicItemReferenceMutator editItemRef(String itemId)
      {
         Objects.requireNonNull(itemId, "item id must not be null.");

         ChangeSet<DataModelV1.BibliographicItemReference> partial = changes.partial(MessageFormat.format("citedItems.{0}", itemId), makeItemRefSelector(itemId));
         return new BibliographicItemReferenceMutatorImpl(itemId, partial);
      }

      @Override
      public void removeItemRef(String itemId)
      {
         Objects.requireNonNull(itemId, "item id must not be null.");

         changes.add(MessageFormat.format("citedItems.{0} [remove]", itemId), citation -> citation.citedItems.removeIf(ref -> ref.itemId == itemId));
      }

      @Override
      public void removeAllItemRefs()
      {
         changes.add("citedItems [clear]", citation -> citation.citedItems.clear());
      }

      /**
       * @param itemId The referenced item id of the item reference to select
       * @return A function that, when given a citation instance, returns the bibliographic item reference that points to the given {@code itemId} or throws an {@link IllegalArgumentException} if no such item reference exists on the citation.
       */
      private Function<DataModelV1.Citation, DataModelV1.BibliographicItemReference> makeItemRefSelector(String itemId)
      {
         return citation -> {
            List<DataModelV1.BibliographicItemReference> matchingRefs = citation.citedItems.stream()
                  .filter(ref -> ref.itemId == itemId)
                  .collect(Collectors.toList());

            if (matchingRefs.isEmpty())
               throw new IllegalArgumentException(MessageFormat.format("citation {0} does not contain item reference {1}", citationId, itemId));
            else if (matchingRefs.size() > 1)
               logger.warning(() -> MessageFormat.format("data corruption: citation {0} contains multiple references to item {1}. selecting first instance", citationId, itemId));

            return matchingRefs.get(0);
         };
      }
   }

   private static class BibliographicItemReferenceMutatorImpl implements BibliographicItemReferenceMutator
   {
      private final String itemId;
      private final ChangeSet<DataModelV1.BibliographicItemReference> changes;

      public BibliographicItemReferenceMutatorImpl(String itemId, ChangeSet<DataModelV1.BibliographicItemReference> changes)
      {
         this.itemId = itemId;
         this.changes = changes;
      }

      @Override
      public String getItemId()
      {
         return itemId;
      }

      @Override
      public void setLocatorType(java.lang.String locatorType)
      {
         changes.add("locatorType", ref -> ref.locatorType = locatorType);
      }

      @Override
      public void setLocator(java.lang.String locator)
      {
         changes.add("locator", ref -> ref.locator = locator);
      }

      @Override
      public void setLabel(java.lang.String label)
      {
         changes.add("label", ref -> ref.label = label);
      }

      @Override
      public void supppressAuthorName(boolean suppress)
      {
         changes.add("suppressAuthor", ref -> ref.suppressAuthor = suppress);
      }
   }

   private static class BibliographicItemMutatorImpl implements BibliographicItemMutator
   {
      private final String itemId;
      private final ChangeSet<DataModelV1.BibliographicItem> changes;

      public BibliographicItemMutatorImpl(String itemId, ChangeSet<DataModelV1.BibliographicItem> changes)
      {
         this.itemId = itemId;
         this.changes = changes;
      }

      @Override
      public String getId()
      {
         return itemId;
      }

      @Override
      public void setType(String type)
      {
         changes.add("type", item -> item.type = type);
      }

      @Override
      public BibliographicItemMetaMutator editMetadata()
      {
         ChangeSet<DataModelV1.BibliographicItemMeta> partial = changes.partial("meta", item -> item.meta);
         return new BibliographicItemMetaMutatorImpl(partial);
      }

      @Override
      public void setCreators(List<BibliographicItemMutator.Creator> creators)
      {
         Objects.requireNonNull(creators, "creators must not be null");

         changes.add("creators", item -> {
            item.creators.clear();
            creators.stream()
                  .map(BibliographicItemMutatorImpl::adaptCreator)
                  .forEach(item.creators::add);
         });
      }

      @Override
      public void removeAllCreators()
      {
         changes.add("creators [clear]", item -> item.creators.clear());
      }

      @Override
      public void setField(String field, String value)
      {
         Objects.requireNonNull(field, "field must not be null");

         changes.add(MessageFormat.format("fields.{0}", field), item -> item.fields.put(field, value));
      }

      @Override
      public void setAllFields(Map<String, String> fields)
      {
         Objects.requireNonNull(fields, "fields must not be null");

         changes.add("fields", item -> {
            item.fields.clear();
            item.fields.putAll(fields);
         });
      }

      @Override
      public void unsetField(String field)
      {
         Objects.requireNonNull(field, "field must not be null");

         changes.add(MessageFormat.format("fields.{0} [remove]", field), item -> item.fields.remove(field));
      }

      @Override
      public void unsetAllFields()
      {
         changes.add("fields [clear]", item -> item.fields.clear());
      }

      /**
       * Adapts a creator DTO used for passing in data into a data persistence object.
       * @param dto
       * @return
       */
      private static DataModelV1.Creator adaptCreator(BibliographicItemMutator.Creator dto)
      {
         DataModelV1.Creator creator = new DataModelV1.Creator();

         creator.role = dto.role;
         creator.firstName = dto.firstName;
         creator.lastName = dto.lastName;

         return creator;
      }
   }

   private static class BibliographicItemMetaMutatorImpl implements BibliographicItemMetaMutator
   {
      private final ChangeSet<DataModelV1.BibliographicItemMeta> changes;

      public BibliographicItemMetaMutatorImpl(ChangeSet<DataModelV1.BibliographicItemMeta> changes)
      {
         this.changes = changes;
      }

      @Override
      public void setKey(String key)
      {
         changes.add("key", meta -> meta.key = key);
      }

      @Override
      public void setCreatorSummary(String creatorSummary)
      {
         changes.add("creatorSummary", meta -> meta.creatorSummary = creatorSummary);
      }

      @Override
      public void setParsedDate(String parsedDate)
      {
         changes.add("parsedDate", meta -> meta.parsedDate = parsedDate);
      }

      @Override
      public void setDateAdded(String dateAdded)
      {
         changes.add("dateAdded", meta -> meta.dateAdded = dateAdded);
      }

      @Override
      public void setDateModified(String dateModified)
      {
         changes.add("dateModified", meta -> meta.dateModified = dateModified);
      }
   }
}
