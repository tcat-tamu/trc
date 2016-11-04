package edu.tamu.tcat.trc.entries.core.repo;

import static java.text.MessageFormat.format;

import java.time.Instant;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.UpdateContext;
import edu.tamu.tcat.trc.repo.id.IdFactory;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolver;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

/**
 * Utility class designed to be used by {@link EntryRepository} implementations to delegate
 * core API functions.
 *
 * @param <EntryType>
 * @param <StorageType>
 * @param <EditCommandType>
 */
public class BasicRepoDelegate<EntryType, StorageType, EditCommandType extends EditEntryCommand<EntryType>>
{
   private final static Logger logger = Logger.getLogger(BasicRepoDelegate.class.getName());

   private final String entryName;
   private final IdFactory idFactory;
   private final EntryResolverRegistry resolvers;
   private final Function<StorageType, EntryType> adapter;

   private final DocumentRepository<EntryType, StorageType, EditCommandType> repo;

   private final ConcurrentHashMap<UUID, EntryRepository.UpdateObserver<EntryType>> observers =
         new ConcurrentHashMap<>();

   public BasicRepoDelegate(String entryName,
                            IdFactory idFactory,
                            EntryResolverRegistry resolvers,
                            Function<StorageType, EntryType> adapter,
                            DocumentRepository<EntryType, StorageType, EditCommandType> repo)
   {
      this.entryName = entryName;
      this.idFactory = idFactory;
      this.resolvers = resolvers;
      this.adapter = adapter;
      this.repo = repo;

      repo.afterUpdate(this::notify);
   }

   public DocumentRepository<EntryType, StorageType, EditCommandType> getDocumentRepo()
   {
      return repo;
   }

   public void dispose()
   {
   }

   @Deprecated // use getOptionally
   public EntryType get(Account account, String id) throws NoSuchEntryException
   {
      String msg = "Unable to find {0} with id [{1}].";
      return getOptionally(account, id)
            .orElseThrow(() -> new NoSuchEntryException(format(msg, entryName, id)));

   }

   public Optional<EntryType> getOptionally(Account account, String id) throws NoSuchEntryException
   {
      return repo.get(id);
   }

   public Iterator<EntryType> listAll()
   {
      try
      {
         return repo.listAll();
      }
      catch (RepositoryException e)
      {
         throw new IllegalStateException(format("Unable to list all {0} entries", entryName), e);
      }
   }

   public EditCommandType create(Account account)
   {
      return repo.create(account, idFactory.get());
   }

   public EditCommandType create(Account account, String id)
   {
      return repo.create(account, id);
   }

   public EditCommandType edit(Account account, String id)
   {
      try
      {
         return repo.edit(account, id);
      }
      catch (RepositoryException e)
      {
         throw new IllegalArgumentException("Unable to edit {0} with id [{1}].", e);
      }
   }

   public CompletableFuture<Boolean> remove(Account account, String id) throws RepositoryException
   {
      try
      {
         return repo.delete(account, id);
      }
      catch (Exception e)
      {
         String msg = "Encountered an unexpected error trying to delete {0} with id [{1}].";
         throw new IllegalStateException(format(msg, entryName, id), e);
      }
   }

   public EntryRepository.ObserverRegistration onUpdate(EntryRepository.UpdateObserver<EntryType> observer, Account account)
   {
      UUID id = UUID.randomUUID();
      observers.put(id, observer);

      return () -> {
        observers.remove(id);
      };
   }

   private void notify(UpdateContext<StorageType> ctx)
   {
      BasicEntryUpdate update = new BasicEntryUpdate(ctx);
      observers.values().parallelStream().forEach(ears -> ears.entryUpdated(update));
   }

   public static class Builder<EntryType, StorageType, EditCommandType extends EditEntryCommand<EntryType>>
   {

      String entryName;
      IdFactory idFactory;
      EntryResolverRegistry resolvers;
      Function<StorageType, EntryType> adapter;
      DocumentRepository<EntryType, StorageType, EditCommandType> repo;

      public Builder<EntryType, StorageType, EditCommandType> setEntryName(String entryName)
      {
         this.entryName = entryName;
         return this;
      }

      public Builder<EntryType, StorageType, EditCommandType> setIdFactory(IdFactory idFactory)
      {
         this.idFactory = idFactory;
         return this;
      }

      public Builder<EntryType, StorageType, EditCommandType> setEntryResolvers(EntryResolverRegistry resolvers)
      {
         this.resolvers = resolvers;
         return this;
      }

      public Builder<EntryType, StorageType, EditCommandType> setAdapter(Function<StorageType, EntryType> adapter)
      {
         this.adapter = adapter;
         return this;
      }

      public Builder<EntryType, StorageType, EditCommandType> setDocumentRepo(DocumentRepository<EntryType, StorageType, EditCommandType> repo)
      {
         this.repo = repo;
         return this;
      }

      public BasicRepoDelegate<EntryType, StorageType, EditCommandType> build()
      {
         if (idFactory == null)
            idFactory = () -> UUID.randomUUID().toString();

         Objects.requireNonNull(entryName, "No entry name specified.");
         Objects.requireNonNull(resolvers, "No resolver registry provided");     // TODO this should be stitched by the system
         Objects.requireNonNull(adapter, "No adapter from storage type to domain type provided");
         Objects.requireNonNull(repo, "No document repo configured");

         return new BasicRepoDelegate<>(entryName, idFactory, resolvers, adapter, repo);
      }
   }
   private class BasicEntryUpdate implements EntryUpdateRecord<EntryType>
   {
      private final UUID updateId;
      private final Account actor;
      private final UpdateContext.ActionType actionType;
      private final Instant timestamp;

      private final UpdateContext<StorageType> ctx;

      private final ConcurrentHashMap<String, EntryType> original = new ConcurrentHashMap<>();

      public BasicEntryUpdate(UpdateContext<StorageType> ctx)
      {
         this.ctx = ctx;
         updateId = ctx.getUpdateId();
         actor = ctx.getActor();
         actionType = ctx.getActionType();

         timestamp = ctx.getTimestamp();

      }
      @Override
      public UUID getId()
      {
         return updateId;
      }

      @Override
      public Instant getTimestamp()
      {
         return timestamp;
      }

      @Override
      public EntryUpdateRecord.UpdateAction getAction()
      {
         switch (actionType) {
            case CREATE:
               return EntryUpdateRecord.UpdateAction.CREATE;
            case EDIT:
               return EntryUpdateRecord.UpdateAction.UPDATE;
            case REMOVE:
               return EntryUpdateRecord.UpdateAction.REMOVE;
            default:
               logger.log(Level.SEVERE, "");
               return EntryUpdateRecord.UpdateAction.UPDATE;
         }
      }

      @Override
      public Account getActor()
      {
         return actor;
      }

      @Override
      public EntryId getEntryReference()
      {
         // HACK should be able to construct an entry reference from the id; need semantic type of entry.
         EntryType entry = getModifiedState();

         if (entry == null)
            entry = getOriginalState();

         if (entry == null)
            throw new IllegalStateException("No entry reference available for " + ctx.getId());

         EntryResolver<EntryType> resolver = resolvers.getResolver(entry);

         return resolver.makeReference(entry);
      }

      @Override
      public EntryType getModifiedState()
      {
         return original.computeIfAbsent("modified", key -> {
            StorageType modified = ctx.getModified();
            return modified != null ? adapter.apply(modified) : null;
         });
      }

      @Override
      public EntryType getOriginalState()
      {
         return original.computeIfAbsent("original", key -> {
            StorageType original = ctx.getOriginal();
            return original != null ? adapter.apply(original) : null;
         });
      }
   }

}
