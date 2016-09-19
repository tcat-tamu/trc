package edu.tamu.tcat.trc.entries.types.biblio.impl;

import static java.text.MessageFormat.format;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.InvalidReferenceException;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.RepositoryContext;
import edu.tamu.tcat.trc.entries.core.repo.UnauthorziedException;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.postgres.BiblioRepoImpl;
import edu.tamu.tcat.trc.entries.types.biblio.postgres.BiblioRepoService;
import edu.tamu.tcat.trc.entries.types.biblio.postgres.EditWorkCommandFactory;
import edu.tamu.tcat.trc.entries.types.biblio.postgres.ModelAdapter;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditBibliographicEntryCommand;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;

public class BibliographicEntryService
{
   private static final Logger logger = Logger.getLogger(BiblioRepoService.class.getName());

   public static final String ID_CONTEXT_WORKS = "works";
   public static final String ID_CONTEXT_EDITIONS = "editions";
   public static final String ID_CONTEXT_VOLUMES = "volumes";
   public static final String ID_CONTEXT_COPIES = "copies";

   private static final String TABLE_NAME = "works";
   private static final String SCHEMA_DATA_FIELD = "work";


   private RepositoryContext context;
   private SearchServiceManager indexSvcMgr;

   private DocumentRepository<BibliographicEntry, WorkDTO, EditBibliographicEntryCommand> docRepo;
   private BasicRepoDelegate<BibliographicEntry, WorkDTO, EditBibliographicEntryCommand> delegate;

   private EntryResolverRegistry.Registration resolverReg;

   private RepositoryContext.Registration repoReg;


   public BibliographicEntryService()
   {
   }

   public void setRepoContext(RepositoryContext context)
   {
      logger.fine(format("[{0}] setting repository context", getClass().getName()));
      this.context = context;
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

//         initSearch();

         logger.fine("Activated " + getClass().getSimpleName());
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to activate bibliographic entry repository service.", ex);
         throw ex;
      }
   }

   private void initRepo()
   {
      initDocRepo();
      initDelegate();

      this.resolverReg = context.registerResolver(new BibliographicEntryResolver());
      this.repoReg = context.registerRepository(BibliographicEntryRepository.class, account -> new BiblioRepoImpl(delegate, account));
   }

   private void initDocRepo()
   {
      DocRepoBuilder<BibliographicEntry, WorkDTO, EditBibliographicEntryCommand> builder = context.getDocRepoBuilder();
      builder.setTableName(TABLE_NAME);
      builder.setDataColumn(SCHEMA_DATA_FIELD);
      builder.setEditCommandFactory(new EditWorkCommandFactory(context::getIdFactory));
      builder.setDataAdapter(ModelAdapter::adapt);
      builder.setStorageType(WorkDTO.class);

      docRepo = builder.build();
   }

   private void initDelegate()
   {
      BasicRepoDelegate.Builder<BibliographicEntry, WorkDTO, EditBibliographicEntryCommand> delegateBuilder =
            new BasicRepoDelegate.Builder<>();

      delegateBuilder.setEntryName("bibliographic");
      delegateBuilder.setIdFactory(context.getIdFactory(ID_CONTEXT_WORKS));
      delegateBuilder.setEntryResolvers(context.getResolverRegistry());
      delegateBuilder.setAdapter(ModelAdapter::adapt);
      delegateBuilder.setDocumentRepo(docRepo);

      delegate = delegateBuilder.build();
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

//         if (searchReg != null)
//            searchReg.close();

         logger.fine("Stopped " + getClass().getSimpleName());

      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to stop" + getClass().getSimpleName(), ex);
         throw ex;
      }
   }

   private class BibliographicEntryResolver extends EntryResolverBase<BibliographicEntry>
   {

      public BibliographicEntryResolver()
      {
         super(BibliographicEntry.class,
               context.getConfig(),
               BibliographicEntryRepository.ENTRY_URI_BASE,
               BibliographicEntryRepository.ENTRY_TYPE_ID);
      }

      @Override
      public BibliographicEntry resolve(Account account, EntryReference reference) throws InvalidReferenceException
      {
         if (!accepts(reference))
            throw new InvalidReferenceException(reference, "Unsupported reference type.");

         return delegate.get(account, reference.id);
      }

      @Override
      protected String getId(BibliographicEntry relationship)
      {
         return relationship.getId();
      }

      @Override
      public CompletableFuture<Boolean> remove(Account account, EntryReference reference) throws InvalidReferenceException, UnauthorziedException, UnsupportedOperationException
      {
         return delegate.remove(account, reference.id);
      }
   }

}
