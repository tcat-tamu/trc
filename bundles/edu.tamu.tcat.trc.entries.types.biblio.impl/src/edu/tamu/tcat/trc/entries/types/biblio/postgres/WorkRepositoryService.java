package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import static java.text.MessageFormat.format;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.InvalidReferenceException;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.RepositoryContext;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditBibliographicEntryCommand;
import edu.tamu.tcat.trc.entries.types.biblio.search.solr.IndexAdapter;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.SolrSearchAdapter;
import edu.tamu.tcat.trc.search.solr.impl.BasicIndexService;

public class WorkRepositoryService
{
   private static final Logger logger = Logger.getLogger(WorkRepositoryService.class.getName());

   // TODO this needs to be refactored.
   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";

   /** Configuration property key that defines Solr core to be used for relationships. */
   public static final String SOLR_CORE = "trc.entries.biblio.solr-core";


   public static final String ID_CONTEXT_WORKS = "works";
   public static final String ID_CONTEXT_EDITIONS = "editions";
   public static final String ID_CONTEXT_VOLUMES = "volumes";
   public static final String ID_CONTEXT_COPIES = "copies";

   private static final String TABLE_NAME = "works";
   private static final String SCHEMA_ID = "trcWork";
   private static final String SCHEMA_DATA_FIELD = "work";

   private DocumentRepository<BibliographicEntry, WorkDTO, EditBibliographicEntryCommand> repoBackend;

   private IdFactory workIds;
   private ConfigurationProperties config;
   private RepositoryContext context;

   // TODO handle DB schema

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
         logger.info("Activating bibliographic entry repository service.");
         workIds = context.getIdFactory(ID_CONTEXT_WORKS);
         config = context.getConfig();

         context.registerRepository(BibliographicEntryRepository.class, account -> new WorkRepoImpl(account));
         context.registerResolver(new BibliographicEntryResolver());

         repoBackend = context.buildDocumentRepo(TABLE_NAME,
               new EditWorkCommandFactory(context::getIdFactory),
               ModelAdapter::adapt,
               WorkDTO.class);

         // TODO need to wire this into core?
         BasicIndexService<WorkDTO> indexSvc = new BasicIndexService<>(
               config.getPropertyValue(SOLR_API_ENDPOINT, URI.class),
               config.getPropertyValue(SOLR_CORE, String.class),
               WorkRepositoryService::adapt,
               entry -> entry.id);
         repoBackend.afterUpdate(ctx -> SolrSearchAdapter.index(indexSvc, ctx));

         logger.fine("Activating bibliographic entry repository service.");
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to activating bibliographic entry repository service.", ex);

      }
   }

   private static SolrInputDocument adapt(WorkDTO entry)
   {
      try
      {
         return IndexAdapter.createWork(entry);
      }
      catch (SearchException ex)
      {
         throw new IllegalStateException(format("Failed to adapt bibliographic entry [{0}]", entry.id), ex);
      }
   }

   public EditBibliographicEntryCommand createWork(Account account, String id)
   {
      return repoBackend.create(account, id);
   }

   public EditBibliographicEntryCommand editWork(Account account, String workId)
   {
      try
      {
         return repoBackend.edit(account, workId);
      }
      catch (RepositoryException e)
      {
         throw new IllegalArgumentException("Unable to find work with id {" + workId + "}.", e);
      }
   }

   public CompletableFuture<Boolean> remove(Account account, String workId)
   {
      CompletableFuture<Boolean> result = new CompletableFuture<>();
      try {
         result = repoBackend.delete(account, workId);
      }
      catch (Exception e)
      {
         String message = "Encountered an unexpected error while trying to delete work with id [{0}].";
         result.completeExceptionally(new IllegalStateException(format(message, workId), e));
         return result;
      }

      return result;
   }


   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when this service is no longer required.
    */
   public void dispose()
   {
   }

   private class WorkRepoImpl implements BibliographicEntryRepository
   {
      private final Account account;

      WorkRepoImpl(Account account)
      {
         this.account = account;
      }

      @Override
      public Iterator<BibliographicEntry> getAllWorks()
      {
         try
         {
            return repoBackend.listAll();
         }
         catch (RepositoryException e)
         {
            throw new IllegalStateException("Unable to list all works", e);
         }
      }

      @Override
      public BibliographicEntry get(String workId)
      {
         try
         {
            return repoBackend.get(workId);
         }
         catch (RepositoryException e)
         {
            String message = "Unable to find work with id [{0}].";
            throw new IllegalArgumentException(format(message, workId), e);
         }
      }

      @Override
      public EditBibliographicEntryCommand create()
      {
         return create(workIds.get());
      }

      @Override
      public EditBibliographicEntryCommand create(String id)
      {
         return WorkRepositoryService.this.createWork(account, id);
      }

      @Override
      public EditBibliographicEntryCommand edit(String workId)
      {
         return WorkRepositoryService.this.editWork(account, workId);

      }

      @Override
      public CompletableFuture<Boolean> remove(String workId)
      {
         return WorkRepositoryService.this.remove(account, workId);
      }

      @Override
      public Edition getEdition(String workId, String editionId)
      {
         String msg = "Unable to find edition with id [{0}] on work [{1}].";

         BibliographicEntry work = get(workId);
         Edition edition = work.getEdition(editionId);
         if (edition == null)
            throw new IllegalArgumentException(format(msg, editionId, workId));

         return edition;
      }

      @Override
      public Volume getVolume(String workId, String editionId, String volumeId)
      {
         Edition edition = getEdition(workId, editionId);
         Volume volume = edition.getVolume(volumeId);
         if (volume == null)
         {
            throw new IllegalArgumentException("Unable to find volume with id {" + volumeId + "} on edition {" + editionId + "} on work {" + workId + "}.");
         }
         return volume;
      }

      @Override
      public EntryRepository.ObserverRegistration onUpdate(EntryRepository.UpdateObserver<BibliographicEntry> observer)
      {
         // TODO Auto-generated method stub
         return null;
      }
   }

   private class BibliographicEntryResolver extends EntryResolverBase<BibliographicEntry>
   {

      public BibliographicEntryResolver()
      {
         super(BibliographicEntry.class, config, BibliographicEntryRepository.ENTRY_URI_BASE, BibliographicEntryRepository.ENTRY_TYPE_ID);
      }

      @Override
      public BibliographicEntry resolve(Account account, EntryReference reference) throws InvalidReferenceException
      {
         if (!accepts(reference))
            throw new InvalidReferenceException(reference, "Unsupported reference type.");

         WorkRepoImpl repo = new WorkRepoImpl(account);
         return repo.get(reference.id);
      }

      @Override
      protected String getId(BibliographicEntry relationship)
      {
         return relationship.getId();
      }
   }

}
