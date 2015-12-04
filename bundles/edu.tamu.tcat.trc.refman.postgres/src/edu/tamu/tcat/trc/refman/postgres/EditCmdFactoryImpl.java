package edu.tamu.tcat.trc.refman.postgres;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.refman.EditReferenceCommand;
import edu.tamu.tcat.trc.refman.ReferenceCollection;
import edu.tamu.tcat.trc.refman.dto.BibRefChangeSet;
import edu.tamu.tcat.trc.refman.dto.BibRefDTO;
import edu.tamu.tcat.trc.refman.dto.CreatorDTO;
import edu.tamu.tcat.trc.refman.types.ItemFieldType;
import edu.tamu.tcat.trc.refman.types.ItemType;
import edu.tamu.tcat.trc.refman.types.ItemTypeProvider;
import edu.tamu.tcat.trc.repo.CommitHook;
import edu.tamu.tcat.trc.repo.EditCommandFactory;

public class EditCmdFactoryImpl implements EditCommandFactory<BibRefDTO, EditReferenceCommand>
{
   public EditCmdFactoryImpl()
   {
   }

   @Override
   public EditRefCommand create(String id, CommitHook<BibRefDTO> commitHook)
   {
      return new EditRefCommand(id, null, commitHook);
   }

   @Override
   public EditRefCommand edit(String id, Supplier<BibRefDTO> currentState, CommitHook<BibRefDTO> commitHook)
   {
      return new EditRefCommand(id, currentState, commitHook);
   }

   public static class EditRefCommand implements EditReferenceCommand
   {
      private static final String ERR_UNDEFINED_FIELD = "The supplied field [{0}] is not defined for this item type [{1}]";

      private Account account;
      private ReferenceCollection collection;
      private ItemTypeProvider typeProvider;

      private ItemType itemType;
      private final BibRefChangeSet changes;

      private final Supplier<BibRefDTO> currentState;
      private final CommitHook<BibRefDTO> hook;


      EditRefCommand(String id, Supplier<BibRefDTO> currentState, CommitHook<BibRefDTO> hook)
      {
         this.currentState = currentState;
         this.hook = hook;

         this.changes = new BibRefChangeSet(id);
      }

      public void setReferenceContext(ReferenceCollection collection, Account account)
      {
         this.account = account;
         this.collection = collection;
         this.typeProvider = collection.getTypeProvider();

         initItemType(currentState);
      }

      private void checkInitialized()
      {
         Objects.requireNonNull(account, "The responsible account has not been specified.");
         Objects.requireNonNull(collection, "The containing collection has not been specified.");
         Objects.requireNonNull(typeProvider, "The type provider has not been specified.");
      }

      private void initItemType(Supplier<BibRefDTO> currentState)
      {
         checkInitialized();

         if (currentState == null)
            return;

         // NOTE: if the stored type is no longer valid for this reference, this will fail
         //       on execute unless a new type is specified.
         BibRefDTO dto = currentState.get();
         if (dto == null)
            return;

         if (!dto.collectionId.equals(collection.getId()))
         {
            String msg = "The item to be edited does not belong to this collection {0}.";
            throw new IllegalStateException(MessageFormat.format(msg, collection.getId()));
         }

         // verify that the collection matches.
         if (typeProvider.hasType(dto.type))
         {
            // this.changes.type = dto.type;
            this.itemType = typeProvider.getItemType(dto.type);
         }
      }

      @Override
      public void setType(ItemType type)
      {
         checkInitialized();

         // TODO check to make sure this type exists.
         String typeId = type.getId();
         if (!typeProvider.hasType(typeId))
            throw new IllegalArgumentException(
                  MessageFormat.format("Invalid type supplied. The type {0} is not defined for this type provider.", typeId));

         this.itemType = type;
         this.changes.type = typeId;
      }

      @Override
      public void setField(ItemFieldType field, String value)
      {
         checkInitialized();

         String key = field.getId();
         boolean hasField = this.itemType.getFields()
                                         .parallelStream()
                                         .anyMatch(f -> f.getId().equals(key));
         if (!hasField)
            throw new IllegalArgumentException(
                  MessageFormat.format(ERR_UNDEFINED_FIELD, field.getLabel(), itemType.getLabel()));

         this.changes.values.put(key, value);
      }

      @Override
      public void setCreators(List<CreatorDTO> creators)
      {
         checkInitialized();

         // TODO filter by roles defined for this type?
         this.changes.creators = creators;
      }

      public Set<String> validate()
      {
         checkInitialized();

         // TODO implement this - return empty set if no errors, otherwise return a set of
         //      error messages suitable for display
         return new HashSet<>();
      }

      @Override
      public Future<String> execute()
      {
         checkInitialized();
         Objects.requireNonNull(itemType, "No item type has been set for this reference.");

         // TODO probably need to supply copy constructor to isolate DTOs
         changes.original = (this.currentState != null) ?  this.currentState.get() : null;
         BibRefDTO data = constructUpdatedDto(changes.original);

         return hook.submit(data, changes);
      }

      private String getType(BibRefDTO original)
      {
         String type = changes.type != null ? changes.type.trim() : original.type;
         if (!typeProvider.hasType(type))
         {
            String msg = "Invalid item type {0}. Type is not defined for this collection: {1}";
            throw new IllegalStateException(MessageFormat.format(msg, type, collection));
         }

         return type;
      }

      // NOTE: does not modify original.
      private BibRefDTO constructUpdatedDto(BibRefDTO original)
      {
         BibRefDTO data = new BibRefDTO();
         data.id = changes.id;
         data.collectionId = collection.getId();
         data.values = new HashMap<String, String>();
         data.type = getType(original);

         for (ItemFieldType field : itemType.getFields())
         {
            String fId = field.getId();
            String value = changes.values.containsKey(fId)
                  ? changes.values.get(fId)
                  : original != null ? original.values.get(fId) : null;

            data.values.put(fId, value);
         }

         // TODO filter change set by fields with valid ids

         data.creators = changes.creators != null ? changes.creators : original.creators;
         return data;
      }

   }

}