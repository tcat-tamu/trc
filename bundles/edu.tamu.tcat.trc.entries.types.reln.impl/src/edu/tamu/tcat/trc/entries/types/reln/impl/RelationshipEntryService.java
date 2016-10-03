package edu.tamu.tcat.trc.entries.types.reln.impl;

import static java.text.MessageFormat.format;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.RepositoryContext;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;
import edu.tamu.tcat.trc.entries.types.reln.impl.repo.EditRelationshipCommandFactory;
import edu.tamu.tcat.trc.entries.types.reln.impl.repo.ModelAdapter;
import edu.tamu.tcat.trc.entries.types.reln.impl.repo.RelationshipRepositoryImpl;
import edu.tamu.tcat.trc.entries.types.reln.impl.repo.RelationshipResolver;
import edu.tamu.tcat.trc.entries.types.reln.impl.search.RelnSearchStrategy;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.search.solr.IndexService;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;
import edu.tamu.tcat.trc.search.solr.impl.SolrSearchMediator;

public class RelationshipEntryService
{
   private final static Logger logger = Logger.getLogger(RelationshipEntryService.class.getName());

   public static final String ID_CONTEXT = "relationships";

   private static final String TABLE_NAME = "relationships";
   private static final String SCHEMA_DATA_FIELD = "relationship";

   private BasicRepoDelegate<Relationship, RelationshipDTO, EditRelationshipCommand> delegate;

   private RelationshipTypeRegistry typeReg;

   private RepositoryContext ctx;
   private SearchServiceManager indexSvcMgr;

   private DocumentRepository<Relationship, RelationshipDTO, EditRelationshipCommand> docRepo;

   private EntryResolverRegistry.Registration resolverReg;
   private RepositoryContext.Registration repoReg;
   private EntryRepository.ObserverRegistration searchReg;


   public void setRepoContext(RepositoryContext ctx)
   {
      this.ctx = ctx;
   }

   public void setTypeRegistry(RelationshipTypeRegistry typeReg)
   {
      this.typeReg = typeReg;
   }

   public void setSearchSvcMgr(SearchServiceManager indexSvcFactory)
   {
      logger.fine(format("[{0}] setting search service manager", getClass().getName()));
      this.indexSvcMgr = indexSvcFactory;
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when all dependencies have been provided and the service is ready to run.
    */
   public void activate()
   {
      try
      {
         logger.info("Activating " + getClass().getSimpleName());

         initRepo();

         // make sure these are all set up and fail fast if not.
         Objects.requireNonNull(delegate);
         Objects.requireNonNull(docRepo);
         Objects.requireNonNull(resolverReg);
         Objects.requireNonNull(repoReg);

         initSearch();

         logger.fine("Activated " + getClass().getSimpleName());
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to activate relationship entry repository service.", ex);
         throw ex;
      }
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when this service is no longer required.
    */
   public void dispose()
   {
      try
      {
         logger.info("Stopping " + getClass().getSimpleName());

         resolverReg.unregister();
         repoReg.unregister();
         docRepo.dispose();
         delegate.dispose();

         if (searchReg != null)
            searchReg.close();

         logger.fine("Stopped " + getClass().getSimpleName());

      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to stop" + getClass().getSimpleName(), ex);
         throw ex;
      }
   }


   private void initRepo()
   {
      initDocRepo();
      initDelegate();

      this.resolverReg = ctx.registerResolver(new RelationshipResolver(delegate, ctx.getConfig()));
      this.repoReg = ctx.registerRepository(RelationshipRepository.class, account -> new RelationshipRepositoryImpl(delegate, account));
   }

   private void initDocRepo()
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
      delegateBuilder.setIdFactory(ctx.getIdFactory(ID_CONTEXT));
      delegateBuilder.setEntryResolvers(ctx.getResolverRegistry());
      delegateBuilder.setAdapter(this::adapt);
      delegateBuilder.setDocumentRepo(docRepo);

      delegate = delegateBuilder.build();
   }

   private Relationship adapt(RelationshipDTO dto)
   {
      return ModelAdapter.adapt(dto, typeReg);
   }

   private void initSearch()
   {
      if (indexSvcMgr == null)
      {
         logger.log(Level.WARNING, "Index support has not been configured for " + getClass().getSimpleName());
         return;
      }
      RelnSearchStrategy indexCfg = new RelnSearchStrategy();
      IndexService<Relationship> indexSvc = indexSvcMgr.configure(indexCfg);

      RelationshipRepositoryImpl repo = new RelationshipRepositoryImpl(delegate, null);     // USE SEARCH ACCT
      searchReg = repo.onUpdate(ctx -> SolrSearchMediator.index(indexSvc, ctx));
   }
}
