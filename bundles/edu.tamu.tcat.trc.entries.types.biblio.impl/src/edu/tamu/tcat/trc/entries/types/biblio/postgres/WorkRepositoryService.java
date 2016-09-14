package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.InvalidReferenceException;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.RepositoryContext;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditBibliographicEntryCommand;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;

public class WorkRepositoryService
{
   private static final Logger logger = Logger.getLogger(WorkRepositoryService.class.getName());

   public static final String ID_CONTEXT_WORKS = "works";
   public static final String ID_CONTEXT_EDITIONS = "editions";
   public static final String ID_CONTEXT_VOLUMES = "volumes";
   public static final String ID_CONTEXT_COPIES = "copies";

   private static final String TABLE_NAME = "works";
   private static final String SCHEMA_DATA_FIELD = "work";

   private DocumentRepository<BibliographicEntry, WorkDTO, EditBibliographicEntryCommand> repoBackend;

   private IdFactory workIds;
   private ConfigurationProperties config;
   private RepositoryContext context;

   private BasicRepoDelegate<BibliographicEntry, WorkDTO, EditBibliographicEntryCommand> delegate;

   public void setRepoContext(RepositoryContext context)
   {
      logger.fine("[bibliographic entry repository] setting repository context.");
      this.context = context;
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when all dependencies have been provided and the service is ready to run.
    */
   public void activate(Map<String, Object> params)
   {
      // TODO fix DS stitching
      try
      {
         logger.info("Activating bibliographic entry repository service. . . ");
         workIds = context.getIdFactory(ID_CONTEXT_WORKS);
         config = context.getConfig();

         initDocRepo();
         initDelegate();

         context.registerRepository(BibliographicEntryRepository.class, account -> new WorkRepoImpl(delegate, account));
         context.registerResolver(new BibliographicEntryResolver());

         logger.fine("Activating bibliographic entry repository service.");
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to activate bibliographic entry repository service.", ex);
         throw ex;
      }
   }

   private void initDelegate()
   {
      BasicRepoDelegate.Builder<BibliographicEntry, WorkDTO, EditBibliographicEntryCommand> delegateBuilder =
            new BasicRepoDelegate.Builder<>();

      delegateBuilder.setEntryName("bibliographic");
      delegateBuilder.setIdFactory(workIds);
      delegateBuilder.setEntryResolvers(context.getResolverRegistry());
      delegateBuilder.setAdapter(ModelAdapter::adapt);
      delegateBuilder.setDocumentRepo(repoBackend);

      delegate = delegateBuilder.build();
   }

   private void initDocRepo()
   {
      DocRepoBuilder<BibliographicEntry, WorkDTO, EditBibliographicEntryCommand> builder = context.getDocRepoBuilder();
      repoBackend = builder.setTableName(TABLE_NAME)
             .setDataColumn(SCHEMA_DATA_FIELD)
             .setEditCommandFactory(new EditWorkCommandFactory(context::getIdFactory))
             .setDataAdapter(ModelAdapter::adapt)
             .setStorageType(WorkDTO.class)
             .build();
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when this service is no longer required.
    */
   public void dispose()
   {
      repoBackend.dispose();
   }

   private class BibliographicEntryResolver extends EntryResolverBase<BibliographicEntry>
   {

      public BibliographicEntryResolver()
      {
         super(BibliographicEntry.class,
               config,
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
