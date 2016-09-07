package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.InvalidReferenceException;
import edu.tamu.tcat.trc.entries.core.repo.RepositoryContext;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkIndexService;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.RepositoryException;

public class WorkRepositoryImpl implements WorkRepository
{
   private static final Logger logger = Logger.getLogger(WorkRepositoryImpl.class.getName());

   public static final String ID_CONTEXT_WORKS = "works";
   public static final String ID_CONTEXT_EDITIONS = "editions";
   public static final String ID_CONTEXT_VOLUMES = "volumes";
   public static final String ID_CONTEXT_COPIES = "copies";

   private static final String TABLE_NAME = "works";
   private static final String SCHEMA_ID = "trcWork";
   private static final String SCHEMA_DATA_FIELD = "work";

   private DocumentRepository<Work, WorkDTO, EditWorkCommand> repoBackend;

   private WorkIndexService indexService;

   private IdFactory idFactory;
   private ConfigurationProperties config;
   private RepositoryContext context;

   /**
    * Bind method for search index service dependency (usually called by dependency injection layer)
    *
    * Note that this is an optional dependency.
    *
    * @param workIndexService
    */
   public void setIndexService(WorkIndexService workIndexService)
   {
      this.indexService = workIndexService;
   }

   public void setRepoContext(RepositoryContext context)
   {
      this.context = context;
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when all dependencies have been provided and the service is ready to run.
    */
   public void activate(Map<String, Object> params)
   {
      // TODO refactor to create an account scoped proxy.
      // TODO remove and re-stitch external dependencies
      context.registerRepository(WorkRepository.class, account -> this);
      context.registerResolver(new BibliographicEntryResolver());

      repoBackend = context.buildDocumentRepo(TABLE_NAME,
                           new EditWorkCommandFactory(context::getIdFactory, indexService),
                           ModelAdapter::adapt,
                           WorkDTO.class);
      idFactory = context.getIdFactory(ID_CONTEXT_WORKS);
      config = context.getConfig();
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when this service is no longer required.
    */
   public void dispose()
   {
   }

   @Override
   public Iterator<Work> getAllWorks()
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
   public Work getWork(String workId)
   {
      try
      {
         return repoBackend.get(workId);
      }
      catch (RepositoryException e)
      {
         throw new IllegalArgumentException("Unable to find work with id {" + workId + "}.", e);
      }
   }

   @Override
   public EditWorkCommand createWork()
   {
      String id = idFactory.get();
      return createWork(id);
   }

   @Override
   public EditWorkCommand createWork(String id)
   {
      return repoBackend.create(id);
   }

   @Override
   public EditWorkCommand editWork(String workId)
   {
      try
      {
         return repoBackend.edit(workId);
      }
      catch (RepositoryException e)
      {
         throw new IllegalArgumentException("Unable to find work with id {" + workId + "}.", e);
      }
   }

   @Override
   public void deleteWork(String workId)
   {
      Boolean result;
      try {
         result = repoBackend.delete(workId).get();
      }
      catch (Exception e) {
         throw new IllegalStateException("Encountered an unexpected error while trying to delete work with id {" + workId + "}.", e);
      }

      if (result != null && result.booleanValue() && indexService != null)
      {
         indexService.remove(workId);
      }
      else
      {
         throw new IllegalArgumentException("Unable to find work with id {" + workId + "}.");
      }
   }

   @Override
   public Edition getEdition(String workId, String editionId)
   {
      Work work = getWork(workId);
      Edition edition = work.getEdition(editionId);
      if (edition == null)
      {
         throw new IllegalArgumentException("Unable to find edition with id {" + editionId + "} on work {" + workId + "}.");
      }
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

   private class BibliographicEntryResolver extends EntryResolverBase<Work>
   {

      public BibliographicEntryResolver()
      {
         super(Work.class, config, WorkRepository.ENTRY_URI_BASE, WorkRepository.ENTRY_TYPE_ID);
      }

      @Override
      public Work resolve(Account account, EntryReference reference) throws InvalidReferenceException
      {
         if (!accepts(reference))
            throw new InvalidReferenceException(reference, "Unsupported reference type.");

         return getWork(reference.id);
      }

      @Override
      protected String getId(Work relationship)
      {
         return relationship.getId();
      }
   }
}
