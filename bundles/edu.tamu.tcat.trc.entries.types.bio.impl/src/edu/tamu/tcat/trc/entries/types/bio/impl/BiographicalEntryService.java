package edu.tamu.tcat.trc.entries.types.bio.impl;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.RepositoryContext;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.impl.model.PersonImpl;
import edu.tamu.tcat.trc.entries.types.bio.impl.repo.BioEntryRepoImpl;
import edu.tamu.tcat.trc.entries.types.bio.impl.repo.BioEntryResolver;
import edu.tamu.tcat.trc.entries.types.bio.impl.repo.DataModelV1;
import edu.tamu.tcat.trc.entries.types.bio.impl.repo.EditPersonCommandFactory;
import edu.tamu.tcat.trc.entries.types.bio.impl.search.BioSearchStrategy;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditBiographicalEntryCommand;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.search.solr.IndexService;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;
import edu.tamu.tcat.trc.search.solr.impl.SolrSearchMediator;

public class BiographicalEntryService
{
   // This centralizes the stitching to spin up all components of the biographical service,
   // include the repo and the indexing support

   private static final Logger logger = Logger.getLogger(BiographicalEntryService.class.getName());

   public static final String ID_CONTEXT = "people";
   private static final String TABLE_NAME = "people";
   private static final String SCHEMA_DATA_FIELD = "historical_figure";

   private RepositoryContext ctx;
   private SearchServiceManager indexSvcMgr;

   private DocumentRepository<BiographicalEntry, DataModelV1.Person, EditBiographicalEntryCommand> docRepo;
   private BasicRepoDelegate<BiographicalEntry, DataModelV1.Person, EditBiographicalEntryCommand> delegate;

   private EntryResolverRegistry.Registration resolverReg;
   private RepositoryContext.Registration repoReg;
   private EntryRepository.ObserverRegistration searchReg;

   public void setRepoContext(RepositoryContext ctx)
   {
      this.ctx = ctx;
   }

   public void setSearchSvcMgr(SearchServiceManager indexSvcFactory)
   {
      this.indexSvcMgr = indexSvcFactory;
   }

   public void start()
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
         logger.log(Level.SEVERE, "Failed to activate " + getClass().getSimpleName(), ex);
         throw ex;
      }
   }

   public void stop()
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
      initDocumentStore();
      initDelegate();

      BioEntryResolver resolver = new BioEntryResolver(ctx.getConfig(), delegate);
      resolverReg = ctx.registerResolver(resolver);
      repoReg = ctx.registerRepository(BiographicalEntryRepository.class,
            account -> new BioEntryRepoImpl(delegate, account));
   }

   private void initDocumentStore()
   {
      DocRepoBuilder<BiographicalEntry, DataModelV1.Person, EditBiographicalEntryCommand> builder = ctx.getDocRepoBuilder();
      builder.setTableName(TABLE_NAME);
      builder.setDataColumn(SCHEMA_DATA_FIELD);
      builder.setEditCommandFactory(new EditPersonCommandFactory());
      builder.setDataAdapter(PersonImpl::new);
      builder.setStorageType(DataModelV1.Person.class);
      builder.setEnableCreation(true);

      docRepo = builder.build();
   }

   private void initDelegate()
   {
      BasicRepoDelegate.Builder<BiographicalEntry, DataModelV1.Person, EditBiographicalEntryCommand> delegateBuilder =
            new BasicRepoDelegate.Builder<>();

      delegateBuilder.setEntryName("biographical entry");
      delegateBuilder.setIdFactory(ctx.getIdFactory(ID_CONTEXT));
      delegateBuilder.setEntryResolvers(ctx.getResolverRegistry());
      delegateBuilder.setAdapter(PersonImpl::new);
      delegateBuilder.setDocumentRepo(docRepo);

      delegate = delegateBuilder.build();
   }

   private void initSearch()
   {
      if (indexSvcMgr == null)
      {
         logger.log(Level.WARNING, "Index support has not been configured for " + getClass().getSimpleName());
         return;
      }

      BioSearchStrategy indexCfg = new BioSearchStrategy(ctx.getConfig());
      IndexService<BiographicalEntry> indexSvc = indexSvcMgr.configure(indexCfg);

      BioEntryRepoImpl repo = new BioEntryRepoImpl(delegate, null);     // USE SEARCH ACCT
      searchReg = repo.onUpdate(ctx -> SolrSearchMediator.index(indexSvc, ctx));
   }
}
