package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkIndexService;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.RepositorySchema;
import edu.tamu.tcat.trc.repo.SchemaBuilder;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;

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
   private SqlExecutor sqlExecutor;
   private IdFactoryProvider idFactoryProvider;
   private WorkIndexService indexService;

   private IdFactory idFactory;

   /**
    * Bind method for SQL executor service dependency (usually called by dependency injection layer)
    *
    * @param sqlExecutor
    */
   public void setSqlExecutor(SqlExecutor sqlExecutor)
   {
      this.sqlExecutor = sqlExecutor;
   }

   /**
    * Bind method for ID factory provider service dependency (usually called by dependency injection layer)
    *
    * @param idFactory
    */
   public void setIdFactory(IdFactoryProvider idFactoryProvider)
   {
      this.idFactoryProvider = idFactoryProvider;
   }

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

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when all dependencies have been provided and the service is ready to run.
    */
   public void activate()
   {
      Objects.requireNonNull(sqlExecutor, "No SQL Executor provided.");

      repoBackend = buildDocumentRepository();
      idFactory = idFactoryProvider.getIdFactory(ID_CONTEXT_WORKS);
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when this service is no longer required.
    */
   public void dispose()
   {
      sqlExecutor = null;
   }

   /**
    * @return A new document repository instance for persisting and retrieving works
    */
   private DocumentRepository<Work, WorkDTO, EditWorkCommand> buildDocumentRepository()
   {
      PsqlJacksonRepoBuilder<Work, WorkDTO, EditWorkCommand> repoBuilder = new PsqlJacksonRepoBuilder<>();

      repoBuilder.setDbExecutor(sqlExecutor);
      repoBuilder.setTableName(TABLE_NAME);
      repoBuilder.setEditCommandFactory(new EditWorkCommandFactoryImpl(idFactoryProvider, indexService));
      repoBuilder.setDataAdapter(ModelAdapter::adapt);
      repoBuilder.setSchema(buildSchema());
      repoBuilder.setStorageType(WorkDTO.class);
      repoBuilder.setEnableCreation(true);

      try
      {
         return repoBuilder.build();
      }
      catch (RepositoryException e)
      {
         logger.log(Level.SEVERE, "Failed to construct work repository instance.", e);
      }
      return null;
   }

   /**
    * @return The repository schema
    */
   private RepositorySchema buildSchema()
   {
      SchemaBuilder schemaBuilder = new BasicSchemaBuilder();
      schemaBuilder.setId(SCHEMA_ID);
      schemaBuilder.setDataField(SCHEMA_DATA_FIELD);
      return schemaBuilder.build();
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
      boolean result;
      try {
         result = repoBackend.delete(workId).get();
      }
      catch (Exception e) {
         throw new IllegalStateException("Encountered an unexpected error while trying to delete work with id {" + workId + "}.", e);
      }

      if (result && indexService != null)
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
}
