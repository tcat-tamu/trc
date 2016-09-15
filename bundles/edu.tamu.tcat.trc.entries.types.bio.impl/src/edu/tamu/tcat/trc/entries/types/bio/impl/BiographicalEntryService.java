package edu.tamu.tcat.trc.entries.types.bio.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.EntryUpdateRecord;
import edu.tamu.tcat.trc.entries.core.repo.RepositoryContext;
import edu.tamu.tcat.trc.entries.core.search.SolrSearchMediator;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.impl.model.PersonImpl;
import edu.tamu.tcat.trc.entries.types.bio.impl.repo.BioEntryRepoImpl;
import edu.tamu.tcat.trc.entries.types.bio.impl.repo.BioEntryResolver;
import edu.tamu.tcat.trc.entries.types.bio.impl.repo.DataModelV1;
import edu.tamu.tcat.trc.entries.types.bio.impl.repo.EditPersonCommandFactory;
import edu.tamu.tcat.trc.entries.types.bio.impl.search.BioSearchStrategy;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditPersonCommand;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;
import edu.tamu.tcat.trc.entries.types.bio.search.PeopleQueryCommand;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.search.solr.BasicIndexServiceFactory;
import edu.tamu.tcat.trc.search.solr.IndexService;

public class BiographicalEntryService
{
   // This centralizes the stitching to spin up all components of the biographical service,
   // include the repo and the indexing support

   private static final Logger logger = Logger.getLogger(BiographicalEntryService.class.getName());

   public static final String ID_CONTEXT = "people";
   private static final String TABLE_NAME = "people";
   private static final String SCHEMA_DATA_FIELD = "historical_figure";

   private IdFactory bioIds;

   private RepositoryContext ctx;
   private ConfigurationProperties config;

   private DocumentRepository<Person, DataModelV1.Person, EditPersonCommand> docRepo;
   private BasicRepoDelegate<Person, DataModelV1.Person, EditPersonCommand> delegate;

   private BasicIndexServiceFactory indexSvcFactory;
   private IndexService<Person, PeopleQueryCommand> indexSvc;

   public void setRepoContext(RepositoryContext ctx)
   {
      this.ctx = ctx;
   }

   public void setIndexSvcFactory(BasicIndexServiceFactory indexSvcFactory)
   {
      this.indexSvcFactory = indexSvcFactory;
   }

   public void start()
   {
      try
      {
         logger.info("Activating relationship repository service. . . ");
         this.bioIds = ctx.getIdFactory(ID_CONTEXT);
         this.config = ctx.getConfig();

         initDocumentStore();
         initDelegate();
         initSearch();

         ctx.registerResolver(new BioEntryResolver(config, delegate));
         ctx.registerRepository(PeopleRepository.class, account -> new BioEntryRepoImpl(delegate, account));

         logger.fine("Activated relationship repository service.");

      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to activate relationship repository service.", ex);
         throw ex;
      }
   }

   public void stop()
   {
      docRepo.dispose();
      delegate.dispose();
   }

   private void initDocumentStore()
   {
      DocRepoBuilder<Person, DataModelV1.Person, EditPersonCommand> builder = ctx.getDocRepoBuilder();
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
      BasicRepoDelegate.Builder<Person, DataModelV1.Person, EditPersonCommand> delegateBuilder =
            new BasicRepoDelegate.Builder<>();

      delegateBuilder.setEntryName("relationship");
      delegateBuilder.setIdFactory(bioIds);
      delegateBuilder.setEntryResolvers(ctx.getResolverRegistry());
      delegateBuilder.setAdapter(PersonImpl::new);
      delegateBuilder.setDocumentRepo(docRepo);

      delegate = delegateBuilder.build();
   }

   private void initSearch()
   {
      if (indexSvcFactory == null)
      {
         logger.log(Level.WARNING, "No index support has been configured for " + getClass().getSimpleName());
         return;
      }

      BioSearchStrategy indexCfg = new BioSearchStrategy(config);
      this.indexSvc = indexSvcFactory.getIndexService(indexCfg);


      BioEntryRepoImpl repo = new BioEntryRepoImpl(delegate, null);     // USE SEARCH ACCT
      repo.onUpdate(this::index);
   }

   private void index(EntryUpdateRecord<Person> ctx)
   {
      SolrSearchMediator.index(indexSvc, ctx);
   }
}
