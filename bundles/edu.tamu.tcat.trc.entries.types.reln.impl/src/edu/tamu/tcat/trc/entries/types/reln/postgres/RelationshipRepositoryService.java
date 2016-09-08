package edu.tamu.tcat.trc.entries.types.reln.postgres;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.InvalidReferenceException;
import edu.tamu.tcat.trc.entries.core.repo.RepositoryContext;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipChangeEvent;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.RepositoryException;

public class RelationshipRepositoryService
{
   public static final String ID_CONTEXT = "relationships";

   private static final String TABLE_NAME = "relationships";
   private static final String SCHEMA_ID = "trcRelationship";
   private static final String SCHEMA_DATA_FIELD = "relationship";

   private DocumentRepository<Relationship, RelationshipDTO, EditRelationshipCommand> repoBackend;
   private IdFactory idFactory;
   private RelationshipTypeRegistry typeReg;

   private RepositoryContext ctx;

   public void setRepoContext(RepositoryContext ctx)
   {
      this.ctx = ctx;
   }

   public void setTypeRegistry(RelationshipTypeRegistry typeReg)
   {
      this.typeReg = typeReg;
   }

   public void activate()
   {
      repoBackend = ctx.buildDocumentRepo(TABLE_NAME,
                            new EditRelationshipCommandFactory(typeReg),
                            dto -> ModelAdapter.adapt(dto, typeReg),
                            RelationshipDTO.class);

      ctx.registerResolver(new RelationshipResolver());
      ctx.registerRepository(RelationshipRepository.class,
            account -> new RelationshipRepositoryImpl(account));

      idFactory = ctx.getIdFactory(ID_CONTEXT);
   }

   public void dispose()
   {
      repoBackend.dispose();
   }

   private Relationship get(Account account, String id) throws RepositoryException
   {
      try
      {
         return repoBackend.get(id);
      }
      catch (RepositoryException e)
      {
         throw new IllegalArgumentException("Unable to find relationship with id {" + id + "}.", e);
      }
   }

   private EditRelationshipCommand create(Account account, String id) throws RepositoryException
   {
      return repoBackend.create(account, id);
   }

   private EditRelationshipCommand edit(Account account, String id) throws RepositoryException
   {
      try
      {
         return repoBackend.edit(account, id);
      }
      catch (RepositoryException e)
      {
         throw new IllegalArgumentException("Unable to find relationship with id {" + id + "}.", e);
      }
   }

   private void delete(Account account, String id) throws RepositoryException
   {
      try
      {
         repoBackend.delete(account, id).get(10, TimeUnit.SECONDS);
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Encountered an unexpected error while trying to delete relationship with id {" + id + "}.", e);
      }
   }


   private class RelationshipRepositoryImpl implements RelationshipRepository
   {

      private final Account account;

      RelationshipRepositoryImpl(Account account)
      {
         this.account = account;
      }

      @Override
      public Iterator<Relationship> getAllRelationships()
      {
         try
         {
            return repoBackend.listAll();
         }
         catch (RepositoryException e)
         {
            throw new IllegalStateException("Unable to list all relationships", e);
         }
      }

      @Override
      public Relationship get(String id) throws RepositoryException
      {
         return RelationshipRepositoryService.this.get(account, id);
      }

      @Override
      public EditRelationshipCommand create() throws RepositoryException
      {
         String id = idFactory.get();
         return RelationshipRepositoryService.this.create(account, id);
      }

      @Override
      public EditRelationshipCommand edit(String id) throws RepositoryException
      {
         return RelationshipRepositoryService.this.edit(account, id);
      }

      @Override
      public void delete(String id) throws RepositoryException
      {
         RelationshipRepositoryService.this.delete(account, id);
      }

      @Override
      public AutoCloseable addUpdateListener(Consumer<RelationshipChangeEvent> ears)
      {
         return null;
      }
   }

   private class RelationshipResolver extends EntryResolverBase<Relationship>
   {

      public RelationshipResolver()
      {
         super(Relationship.class, ctx.getConfig(), RelationshipRepository.ENTRY_URI_BASE, RelationshipRepository.ENTRY_TYPE_ID);
      }

      @Override
      public Relationship resolve(Account account, EntryReference reference) throws InvalidReferenceException
      {
         if (!accepts(reference))
            throw new InvalidReferenceException(reference, "Unsupported reference type.");

         return get(account, reference.id);
      }

      @Override
      protected String getId(Relationship relationship)
      {
         return relationship.getId();
      }
   }
}
