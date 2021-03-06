package edu.tamu.tcat.trc.entries.types.bio.impl;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistrar;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.impl.model.PersonImpl;
import edu.tamu.tcat.trc.entries.types.bio.impl.repo.BioEntryRepoImpl;
import edu.tamu.tcat.trc.entries.types.bio.impl.repo.BioEntryResolver;
import edu.tamu.tcat.trc.entries.types.bio.impl.repo.DataModelV1;
import edu.tamu.tcat.trc.entries.types.bio.impl.repo.EditPersonCommandFactory;
import edu.tamu.tcat.trc.entries.types.bio.impl.search.BioSearchStrategy;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditBiographicalEntryCommand;
import edu.tamu.tcat.trc.impl.psql.entries.SolrSearchSupport;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistrar;
import edu.tamu.tcat.trc.search.solr.IndexService;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;

public class BiographicalEntryService
{
   // This centralizes the stitching to spin up all components of the biographical service,
   // include the repo and the indexing support

   private static final Logger logger = Logger.getLogger(BiographicalEntryService.class.getName());

   public static final String ID_CONTEXT = "people";
   private static final String TABLE_NAME = "biographical";
   private static final String TABLE_NAME_PROP = "trc.entries.biographical.db.table";

   private EntryRepositoryRegistrar repoRegistrar;
//    private SearchServiceManager indexSvcMgr;
   private TrcApplication trcCtx;

   private DocumentRepository<BiographicalEntry, EditBiographicalEntryCommand> docRepo;
   private BasicRepoDelegate<BiographicalEntry, DataModelV1.Person, EditBiographicalEntryCommand> delegate;

   private EntryResolverRegistrar.Registration resolverReg;
   private EntryRepositoryRegistrar.Registration repoReg;
   private EntryRepository.ObserverRegistration searchReg;


   public void setRepoContext(EntryRepositoryRegistrar ctx)
   {
      this.repoRegistrar = ctx;
   }

   public void setTrcContext(TrcApplication trcCtx)
   {
      this.trcCtx = trcCtx;
   }

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
         logger.log(Level.SEVERE, "Failed to activate " + getClass().getSimpleName(), ex);
         throw ex;
      }
   }


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

   private String getTableName()
   {
      return trcCtx.getConfig().getPropertyValue(TABLE_NAME_PROP, String.class, TABLE_NAME);
   }

   private void initRepo()
   {
      initDocumentStore();
      initDelegate();

      BioEntryResolver resolver = new BioEntryResolver(repoRegistrar.getConfig(), delegate);
      resolverReg = repoRegistrar.registerResolver(resolver);
      repoReg = repoRegistrar.registerRepository(BiographicalEntryRepository.class,
            account -> new BioEntryRepoImpl(delegate, account));
   }

   private void initDocumentStore()
   {
      DocRepoBuilder<BiographicalEntry, DataModelV1.Person, EditBiographicalEntryCommand> builder = repoRegistrar.getDocRepoBuilder();
      builder.setPersistenceId(getTableName());
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
      delegateBuilder.setIdFactory(trcCtx.getIdFactory(ID_CONTEXT));
      delegateBuilder.setEntryResolvers(trcCtx.getResolverRegistry());
      delegateBuilder.setDocumentRepo(docRepo);

      delegate = delegateBuilder.build();
   }

   private void initSearch()
   {
      SearchServiceManager indexSvcMgr = trcCtx.getSearchManager();
      if (indexSvcMgr == null)
      {
         logger.log(Level.WARNING, "Index support has not been configured for " + getClass().getSimpleName());
         return;
      }

      BioSearchStrategy indexCfg = new BioSearchStrategy(trcCtx);
      IndexService<BiographicalEntry> indexSvc = indexSvcMgr.configure(indexCfg);

      BioEntryRepoImpl repo = new BioEntryRepoImpl(delegate, null);     // USE SEARCH ACCT
      SolrSearchSupport<BiographicalEntry> mediator = new SolrSearchSupport<>(indexSvc, indexCfg);
      searchReg = repo.onUpdate(mediator::handleUpdate);
   }
}
