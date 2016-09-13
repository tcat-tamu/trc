package edu.tamu.tcat.trc.entries.types.reln.postgres;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.InvalidReferenceException;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.RepositoryContext;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;

public class RelationshipRepositoryService
{
   private final static Logger logger = Logger.getLogger(RelationshipRepositoryService.class.getName());

   public static final String ID_CONTEXT = "relationships";

   private static final String TABLE_NAME = "relationships";
   private static final String SCHEMA_DATA_FIELD = "relationship";

   private BasicRepoDelegate<Relationship, RelationshipDTO, EditRelationshipCommand> delegate;

   private IdFactory relnIds;
   private RelationshipTypeRegistry typeReg;

   private RepositoryContext ctx;

   private ConfigurationProperties config;

   private DocumentRepository<Relationship, RelationshipDTO, EditRelationshipCommand> docRepo;


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
      try
      {
         logger.info("Activating relationship repository service. . . ");
         this.relnIds = ctx.getIdFactory(ID_CONTEXT);
         this.config = ctx.getConfig();

         initDocumentStore();
         initDelegate();

         ctx.registerResolver(new RelationshipResolver());
         ctx.registerRepository(RelationshipRepository.class, account -> new RelationshipRepositoryImpl(delegate, account));

         logger.fine("Activated relationship repository service.");

      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to activate relationship repository service.", ex);
         throw ex;
      }
   }

   private void initDocumentStore()
   {
      DocRepoBuilder<Relationship, RelationshipDTO, EditRelationshipCommand> builder = ctx.getDocRepoBuilder();
      builder.setTableName(TABLE_NAME);
      builder.setDataColumn(SCHEMA_DATA_FIELD);
      builder.setEditCommandFactory(new EditRelationshipCommandFactory(typeReg));
      builder.setDataAdapter(this::adapt);
      builder.setStorageType(RelationshipDTO.class);
      builder.setEnableCreation(true);

      docRepo = builder.build();
   }

   private void initDelegate()
   {
      BasicRepoDelegate.Builder<Relationship, RelationshipDTO, EditRelationshipCommand> delegateBuilder =
            new BasicRepoDelegate.Builder<>();

      delegateBuilder.setEntryName("relationship");
      delegateBuilder.setIdFactory(relnIds);
      delegateBuilder.setEntryResolvers(ctx.getResolverRegistry());
      delegateBuilder.setAdapter(this::adapt);
      delegateBuilder.setDocumentRepo(docRepo);

      delegate = delegateBuilder.build();
   }

   private Relationship adapt(RelationshipDTO dto)
   {
      return ModelAdapter.adapt(dto, typeReg);
   }

   public void dispose()
   {
      delegate.dispose();
      docRepo.dispose();
   }


   private class RelationshipResolver extends EntryResolverBase<Relationship>
   {
      public RelationshipResolver()
      {
         super(Relationship.class, config, RelationshipRepository.ENTRY_URI_BASE, RelationshipRepository.ENTRY_TYPE_ID);
      }

      @Override
      public Relationship resolve(Account account, EntryReference reference) throws InvalidReferenceException
      {
         if (!accepts(reference))
            throw new InvalidReferenceException(reference, "Unsupported reference type.");

         return delegate.get(account, reference.id);
      }

      @Override
      protected String getId(Relationship relationship)
      {
         return relationship.getId();
      }
   }
}
