package edu.tamu.tcat.trc.entries.types.reln.impl;

import static java.text.MessageFormat.format;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistrar;
import edu.tamu.tcat.trc.entries.types.reln.GroupedRelationshipSet;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipInferenceStrategy;
import edu.tamu.tcat.trc.entries.types.reln.impl.model.RelationshipImpl;
import edu.tamu.tcat.trc.entries.types.reln.impl.repo.DataModelV1;
import edu.tamu.tcat.trc.entries.types.reln.impl.repo.EditRelationshipCommandFactory;
import edu.tamu.tcat.trc.entries.types.reln.impl.repo.RelationshipResolver;
import edu.tamu.tcat.trc.entries.types.reln.impl.search.RelnSearchStrategy;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.impl.psql.entries.SolrSearchSupport;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistrar;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.search.solr.IndexService;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;

public class RelationshipEntryService
{
   private final static Logger logger = Logger.getLogger(RelationshipEntryService.class.getName());

   public static final String ID_CONTEXT = "relationships";

   private static final String TABLE_NAME = "relationships";
   private static final String SCHEMA_DATA_FIELD = "relationship";

   private BasicRepoDelegate<Relationship, DataModelV1.Relationship, EditRelationshipCommand> delegate;

   private RelationshipTypeRegistry typeReg;

   private EntryRepositoryRegistrar ctx;
   private SearchServiceManager indexSvcMgr;

   private DocumentRepository<Relationship, DataModelV1.Relationship, EditRelationshipCommand> docRepo;

   private EntryResolverRegistrar.Registration resolverReg;
   private EntryRepositoryRegistrar.Registration repoReg;
   private EntryRepository.ObserverRegistration searchReg;

   private EntryResolverRegistry resolvers;


   public void setRepoContext(EntryRepositoryRegistrar ctx)
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

         resolvers = ctx.getResolverRegistry();

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
      this.repoReg = ctx.registerRepository(RelationshipRepository.class, account -> new RelationshipRepositoryImpl(account));
   }

   private void initDocRepo()
   {
      DocRepoBuilder<Relationship, DataModelV1.Relationship, EditRelationshipCommand> builder = ctx.getDocRepoBuilder();
      builder.setTableName(TABLE_NAME);
      builder.setDataColumn(SCHEMA_DATA_FIELD);
      builder.setEditCommandFactory(new EditRelationshipCommandFactory(typeReg, resolvers));
      builder.setDataAdapter(this::adapt);
      builder.setStorageType(DataModelV1.Relationship.class);
      builder.setEnableCreation(true);

      docRepo = builder.build();
   }

   private void initDelegate()
   {
      BasicRepoDelegate.Builder<Relationship, DataModelV1.Relationship, EditRelationshipCommand> delegateBuilder =
            new BasicRepoDelegate.Builder<>();

      delegateBuilder.setEntryName("relationship");
      delegateBuilder.setIdFactory(ctx.getIdFactory(ID_CONTEXT));
      delegateBuilder.setEntryResolvers(resolvers);
      delegateBuilder.setAdapter(this::adapt);
      delegateBuilder.setDocumentRepo(docRepo);

      delegate = delegateBuilder.build();
   }

   private Relationship adapt(DataModelV1.Relationship dto)
   {
      return new RelationshipImpl(dto, typeReg, resolvers);
   }

   private void initSearch()
   {
      if (indexSvcMgr == null)
      {
         logger.log(Level.WARNING, "Index support has not been configured for " + getClass().getSimpleName());
         return;
      }
      RelnSearchStrategy indexCfg = new RelnSearchStrategy(resolvers);
      IndexService<Relationship> indexSvc = indexSvcMgr.configure(indexCfg);

      RelationshipRepositoryImpl repo = new RelationshipRepositoryImpl(null);     // USE SEARCH ACCT
      SolrSearchSupport<Relationship> mediator = new SolrSearchSupport<>(indexSvc, indexCfg);
      searchReg = repo.onUpdate(mediator::handleUpdate);
   }

   public class RelationshipRepositoryImpl implements RelationshipRepository
   {
      private final Account account;

      public RelationshipRepositoryImpl(Account account)
      {
         this.account = account;
      }

      @Override
      public RelationshipTypeRegistry getTypeRegistry()
      {
         return typeReg;
      }

      @Override
      public Relationship get(String id)
      {
         return delegate.get(account, id);
      }

      @Override
      public Iterator<Relationship> listAll()
      {
         return delegate.listAll();
      }

      @Override
      public EditRelationshipCommand create()
      {
         return delegate.create(account);
      }

      @Override
      public EditRelationshipCommand create(String id)
      {
         return delegate.create(account, id);
      }

      @Override
      public EditRelationshipCommand edit(String id)
      {
         return delegate.edit(account, id);
      }

      @Override
      public CompletableFuture<Boolean> remove(String id)
      {
         return delegate.remove(account, id);
      }

      @Override
      public EntryRepository.ObserverRegistration onUpdate(EntryRepository.UpdateObserver<Relationship> observer)
      {
         return delegate.onUpdate(observer, account);
      }

      private final List<RelationshipInferenceStrategy> inferenceStrategies =
            new CopyOnWriteArrayList<>();

      @Override
      public void register(RelationshipInferenceStrategy strategy)
      {
         inferenceStrategies.add(strategy);
      }

      @Override
      public GroupedRelationshipSet getRelationships(EntryId ref)
      {
         return getRelationships(ref, reln -> true);
      }

//      @Override
      public GroupedRelationshipSet getRelationships(EntryId ref, Predicate<Relationship> filter)
      {
         inferenceStrategies.stream()
            .filter(strategy -> strategy.accepts(ref))
            .flatMap(strategy -> strategy.getRelationships(ref))
            .filter(filter);

         // TODO Auto-generated method stub
         return null;
      }
   }
}
