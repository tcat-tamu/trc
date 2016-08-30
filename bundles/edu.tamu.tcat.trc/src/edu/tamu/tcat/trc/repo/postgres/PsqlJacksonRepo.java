package edu.tamu.tcat.trc.repo.postgres;

import static edu.tamu.tcat.trc.repo.DocumentRepository.unwrap;
import static java.text.MessageFormat.format;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.EditCommandFactory.UpdateStrategy;
import edu.tamu.tcat.trc.repo.EntryUpdateObserver;
import edu.tamu.tcat.trc.repo.NoSuchEntryException;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.RepositorySchema;
import edu.tamu.tcat.trc.repo.UpdateContext;
import edu.tamu.tcat.trc.repo.UpdateContext.ActionType;

public class PsqlJacksonRepo<RecordType, DTO, EditCommandType> implements DocumentRepository<RecordType, DTO, EditCommandType>
{
   private static final Logger logger = Logger.getLogger(PsqlJacksonRepo.class.getName());

   private SqlExecutor exec;
   private Supplier<String> idFactory;

   private String tablename;
   private RepositorySchema schema;
   private Function<DTO, RecordType> adapter;
   private Class<DTO> storageType;

   private EditCommandFactory<DTO, EditCommandType> cmdFactory;

   private String getRecordSql;
   private String createRecordSql;
   private String updateRecordSql;
   private String removeRecordSql;

   private final Map<UUID, EntryUpdateObserver<DTO>> preCommitTasks = new ConcurrentHashMap<>();
   private final Map<UUID, EntryUpdateObserver<DTO>> postCommitTasks = new ConcurrentHashMap<>();

   private LoadingCache<String, RecordType> cache;

   PsqlJacksonRepo()
   {
   }

   void setSqlExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   void setIdFactory(Supplier<String> idFactory)
   {
      this.idFactory = idFactory;
   }

   void setTableName(String tablename)
   {
      this.tablename = tablename;
   }


   void setSchema(RepositorySchema schema)
   {
      this.schema = schema;
   }

   void setCommandFactory(EditCommandFactory<DTO, EditCommandType> cmdFactory)
   {
      this.cmdFactory = cmdFactory;
   }

   void setAdapter(Function<DTO, RecordType> adapter)
   {
      this.adapter = adapter;
   }

   void setStorageType(Class<DTO> storageType)
   {
      this.storageType = storageType;
   }

   void activate()
   {
      // default to UUID-based ids
      if (idFactory == null)
         idFactory = () -> UUID.randomUUID().toString();

      Objects.requireNonNull(exec, "The SQL executor has not bee supplied");
      Objects.requireNonNull(tablename, "The tablename has not bee supplied");
      if (tablename.trim().isEmpty())
         throw new IllegalStateException("The tablename must not be an empty string");

      Objects.requireNonNull(cmdFactory, "The edit command factory has not bee supplied");
      Objects.requireNonNull(schema, "The data schema has not bee supplied");
      Objects.requireNonNull(adapter, "The data adapter has not bee supplied");
      Objects.requireNonNull(storageType, "The storage type has not bee supplied");

      logger.info(format("Initializing document repository for schema {0} using table {1}", schema.getId(), tablename));

      this.getRecordSql = prepareGetSql();
      this.createRecordSql = prepareInsertSql();
      this.updateRecordSql = prepareUpdateSql();
      this.removeRecordSql = prepareRemoveSql();

      this.initCache();
   }

   @Override
   public void dispose()
   {
      this.cache.invalidateAll();
      this.cache = null;
   }

   private void initCache()
   {
      cache = CacheBuilder.newBuilder()
                     .maximumSize(1000)
                     .expireAfterAccess(10, TimeUnit.MINUTES)
                     .build(new CacheLoaderImpl());
   }

   private class CacheLoaderImpl extends CacheLoader<String, RecordType>
   {
      @Override
      public RecordType load(String key) throws Exception
      {
         DTO dto = loadStoredRecord(key);
         RecordType record = adapter.apply(dto);
         return record;
      }
   }

   public String buildNotRemovedClause()
   {
      String removedField = schema.getRemovedField();
      return (removedField != null) ? format("AND {0} IS NULL", removedField) : "";
   }

   private String prepareGetSql()
   {
      String GET_RECORD_SQL = "SELECT {0} FROM {1} WHERE {2} = ? {3}";
      return format(GET_RECORD_SQL, schema.getDataField(), tablename, schema.getIdField(), buildNotRemovedClause());
   }

   private String prepareInsertSql()
   {
      String INSERT_SQL = "INSERT INTO {0} ({1}, {2}) VALUES(?, ?)";

      return format(INSERT_SQL, tablename, schema.getDataField(), schema.getIdField());
   }

   private String prepareUpdateSql()
   {
      String UPDATE_SQL = "UPDATE {0} SET {1} = ?{2} WHERE {3} = ?";

      String idCol = schema.getIdField();
      String dataCol = schema.getDataField();
      String modifiedCol = schema.getModifiedField();
      String dateModClause = hasDateModifiedField() ? ", " + modifiedCol + " = now()": "";
      return format(UPDATE_SQL, tablename, dataCol, dateModClause, idCol);
   }

   private String prepareRemoveSql()
   {
      String MARK_REMOVED_SQL =  "UPDATE {0} SET {1} = now(){2} WHERE {3} = ?";
      String DELETE_SQL =  "DELETE FROM {0} WHERE {1} = ?";

      String idField = schema.getIdField();
      String removedField = schema.getRemovedField();
      if (removedField != null)
      {
         String modifiedField = schema.getModifiedField();
         String dateModClause = hasDateModifiedField() ? ", " + modifiedField + " = now()": "";
         return format(MARK_REMOVED_SQL, tablename, removedField, dateModClause, idField);
      }
      else
      {
         return format(DELETE_SQL, tablename, idField);
      }

   }

   private boolean hasDateModifiedField()
   {
      String modifiedField = schema.getModifiedField();
      return modifiedField != null && !modifiedField.trim().isEmpty();
   }

   @Override
   public Iterator<RecordType> listAll()
   {
      return new PagedRecordIterator<>(this::getPersonBlock, this::parse, 100);
   }

   @Override
   public RecordType get(String id) throws RepositoryException
   {
      try
      {
         return cache.get(id);
      }
      catch (ExecutionException | UncheckedExecutionException ex)
      {
         Throwable cause = ex.getCause();
         if (cause instanceof NoSuchEntryException)
            throw (NoSuchEntryException)cause;
         if (cause instanceof RepositoryException)
            throw (RepositoryException)cause;
         if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;

         throw new IllegalStateException("Failed to retrieve record [" + id + "]", cause);
      }
   }

   @Override
   public Collection<RecordType> get(String... ids) throws RepositoryException
   {
      // HACK: this is potentially very inefficient. Should load records that are already in the
      //       cache and then execute a query that will load all remaining records from the DB
      //       in a single task (depending on the number of ids, possibly via multiple queries).

      HashMap<String, RecordType> results = new HashMap<>();
      for (String id: ids)
      {
         if (results.containsKey(id))
            continue;

         results.put(id, get(id));
      }

      return Collections.unmodifiableCollection(results.values());
   }

   @Override
   public EditCommandType create(Account account)
   {
      String id = idFactory != null
            ? idFactory.get()
            : UUID.randomUUID().toString();
      return create(account, id);
   }

   @Override
   public EditCommandType create(Account account, String id) throws UnsupportedOperationException
   {
      UpdateContextImpl context = new UpdateContextImpl(id, ActionType.CREATE, account, () -> null);
      UpdateStrategyImpl updater = new UpdateStrategyImpl(context, (dto) -> doCreate(id, dto));

      return this.cmdFactory.create(id, updater);
   }

   @Override
   public EditCommandType edit(Account account, String id) throws RepositoryException
   {
      UpdateContextImpl context = new UpdateContextImpl(id, ActionType.EDIT, account, () -> loadStoredRecord(id));
      UpdateStrategyImpl updater = new UpdateStrategyImpl(context, (dto) -> doEdit(id, dto));

      return this.cmdFactory.edit(id, updater);
   }

   @Override
   public CompletableFuture<Boolean> delete(Account account, String id) throws UnsupportedOperationException
   {
      UpdateContextImpl context = new UpdateContextImpl(id, ActionType.REMOVE, null, () -> null);
      UpdateStrategyImpl updater = new UpdateStrategyImpl(context, (dto) -> doDelete(id));

      return updater.update(dto -> null).thenApply(dto -> Boolean.TRUE);
   }

   @Override
   public Runnable beforeUpdate(EntryUpdateObserver<DTO> preCommitTask)
   {
      UUID observerId = UUID.randomUUID();
      preCommitTasks.put(observerId, preCommitTask);

      return () -> preCommitTasks.remove(observerId);
   }

   @Override
   public Runnable afterUpdate(EntryUpdateObserver<DTO> postCommitTask)
   {
      UUID observerId = UUID.randomUUID();
      postCommitTasks.put(observerId, postCommitTask);

      return () -> postCommitTasks.remove(observerId);
   }

   private RecordType parse(String json)
   {
      try
      {
         ObjectMapper mapper = getObjectMapper();

         DTO dto = mapper.readValue(json, storageType);
         return adapter.apply(dto);
      }
      catch (IOException ex)
      {
         String message = format("Failed to parse stored record data.\n\t{0}", json);
         throw new IllegalStateException(message, ex);
      }
   }

   private Future<List<String>> getPersonBlock(int offset, int limit)
   {

      return exec.submit((conn) -> getPageBlock(conn, offset, limit));
   }

   private List<String> getPageBlock(Connection conn, int offset, int limit) throws InterruptedException
   {
      if (Thread.interrupted())
         throw new InterruptedException();

      String template = "SELECT {0} FROM {1} ORDER BY {2} LIMIT {3} OFFSET {4}";
      String sql = format(template, schema.getDataField(), tablename,
            schema.getIdField(), Integer.toString(limit), Integer.toString(offset));

      List<String> jsonData = new ArrayList<>();
      try (Statement stmt = conn.createStatement())
      {
         ResultSet rs = stmt.executeQuery(sql);
         while (rs.next())
         {
            if (Thread.interrupted())
               throw new InterruptedException();

            PGobject pgo = (PGobject)rs.getObject(schema.getDataField());
            String json = pgo.toString();
            jsonData.add(json);
         }

         return jsonData;
      }
      catch (SQLException e)
      {
         throw new IllegalStateException("Faield to retrieve person.", e);
      }
   }

   /**
    * @return the JSON representation associated with this id.
    */
   private DTO loadStoredRecord(String id) throws RepositoryException
   {
      ObjectMapper mapper = getObjectMapper();

      Future<DTO> future = exec.submit((conn) -> {
         if (Thread.interrupted())
            throw new InterruptedException();

         String json = loadJson(conn, id);
         return mapper.readValue(json, storageType);
      });

      return unwrap(future, () -> format("Failed to load DTO for entry with id={0}", id));
   }

   /**
    * Called from within the database executor to retrieve the underlying JSON representation
    * of an item.
    *
    * @param conn
    * @param id
    * @return
    *
    * @throws NoSuchEntryException If there is no entry for the supplied id
    *       or if that entry has been flagged as removed.
    * @throws RepositoryException If an unknown internal error occurred.
    * @throws InterruptedException If the execution was interrupted
    */
   private String loadJson(Connection conn, String id)
         throws NoSuchEntryException, RepositoryException
   {
      try (PreparedStatement ps = conn.prepareStatement(getRecordSql))
      {
         ps.setString(1, id);
         try (ResultSet rs = ps.executeQuery())
         {
            if (!rs.next())
               throw new NoSuchEntryException("Could not find record for id = '" + id + "'");

            PGobject pgo = (PGobject)rs.getObject(schema.getDataField());
            return pgo.toString();
         }
      }
      catch (SQLException e)
      {
         throw new RepositoryException("Faield to retrieve the record.", e);
      }
   }

   private CompletableFuture<DTO> doCreate(String id, DTO record)
   {
      return exec.submit((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(createRecordSql))
         {
            PGobject json = asJson(record);

            ps.setObject(1, json);
            ps.setString(2, id);

            int ct = ps.executeUpdate();
            if (ct != 1)
               throw new IllegalStateException("Failed to create record for id [" + id + "]. Unexpected number of rows updates [" + ct + "]");

            cache.invalidate(id);
         }
         catch (IOException e)
         {
            // NOTE this is an internal configuration error. The JsonMapper should be configured to
            //      serialize HistoricalFigureDV instances correctly.
            throw new IllegalStateException("Failed to serialize the supplied record [" + record + "]", e);
         }

         return record;
      });
   }

   private CompletableFuture<DTO> doDelete(String id)
   {
      return exec.submit((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(removeRecordSql))
         {
            ps.setString(1, id);
            ps.executeUpdate();
            cache.invalidate(id);
            // boolean removed = Boolean.valueOf(ct == 1);

            return null;
         }
         catch (SQLException e)
         {
            String message = format("Failed to mark record as deleted. Record id: {0}", id);
            throw new IllegalStateException(message, e);
         }
      });
   }

   private CompletableFuture<DTO> doEdit(String id, DTO record)
   {
      return exec.submit((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(updateRecordSql))
         {
            ps.setObject(1, asJson(record));
            ps.setString(2, id);

            int ct = ps.executeUpdate();
            if (ct != 1)
               throw new IllegalStateException("Failed to update record for id [" + id + "]. Unexpected number of rows updates [" + ct + "]");

            cache.invalidate(id);
            return record;
         }
         catch (IOException e)
         {
            throw new IllegalStateException("Failed to serialize the supplied record [" + record + "]", e);
         }
      });
   }

   private PGobject asJson(DTO record) throws JsonProcessingException, SQLException
   {
      ObjectMapper mapper = getObjectMapper();
      String entryJson = mapper.writeValueAsString(record);

      PGobject json = new PGobject();
      json.setType("json");
      json.setValue(entryJson);
      return json;
   }

   private ObjectMapper getObjectMapper()
   {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      return mapper;
   }

   private class UpdateStrategyImpl implements UpdateStrategy<DTO>
   {
      private final UpdateContextImpl context;
      private final Function<DTO, CompletableFuture<DTO>> updateAction;

      /**
       *
       * @param entryId The id of the object being updated.
       * @param updateAction The persistence action to invoke (typically create, update or delete)
       *       on the DTO object submitted by the edit command.
       */
      UpdateStrategyImpl(UpdateContextImpl context, Function<DTO, CompletableFuture<DTO>> updateAction)
      {
         this.context = context;
         this.updateAction = updateAction;
      }

      @Override
      public CompletableFuture<DTO> update(Function<UpdateContext<DTO>, DTO> generator)
      {
         context.start();

         DTO dto = generator.apply(context);
         context.modified.complete(dto);

         try
         {
            preCommitTasks.entrySet().parallelStream()
               .forEach(entry -> firePreCommitTask(entry.getKey(), entry.getValue()));
         }
         catch (Exception ex)
         {
            String template = "Aborting update for entry {0}. Update id: {1}\n\tReason: {2}";
            String msg = format(template, context.getId(), context.getUpdateId(), ex.getMessage());
            logger.log(Level.WARNING, msg, ex);

            context.addError(msg);
            CompletableFuture<DTO> failure = new CompletableFuture<>();
            failure.completeExceptionally(ex);
            return failure;
         }

         CompletableFuture<DTO> result = updateAction.apply(dto);

         // fire post-commit hooks]
         result
            .thenRun(() -> context.timestamp = Instant.now())
            .thenRunAsync(() -> {
               postCommitTasks.entrySet().parallelStream()
                              .forEach(entry -> firePostCommitTask(entry.getKey(), entry.getValue()));
            })
            .thenRun(() -> context.finish());

         return result;
      }

      private void firePreCommitTask(UUID taskId, EntryUpdateObserver<DTO> task)
      {
         task.notify(context);
      }

      private void firePostCommitTask(UUID taskId, EntryUpdateObserver<DTO> task)
      {
         try
         {
            task.notify(context);
         }
         catch (Exception ex)
         {
            String template = "Post-commit task failed while following update of entry {0}. Update id: {1}\n\tReason: {2}";
            String msg = format(template, context.getId(), context.getUpdateId(), ex.getMessage());
            context.addError(msg);
            logger.log(Level.WARNING, msg, ex);
         }
      }
   }

   private class UpdateContextImpl implements UpdateContext<DTO>
   {
      private final UUID updateId = UUID.randomUUID();
      private Instant timestamp = null;
      private final String entryId;
      private final ActionType action;
      private final Account actor;
      private final Supplier<DTO> supplier;

      private DTO initial;
      private CompletableFuture<DTO> original;
      private CompletableFuture<DTO> modified = new CompletableFuture<>();

      private final List<String> errors = new CopyOnWriteArrayList<>();

      UpdateContextImpl(String id, ActionType action, Account actor, Supplier<DTO> supplier)
      {
         this.entryId = id;
         this.action = action;
         this.actor = actor;
         this.supplier = supplier;

         this.initial = supplier.get();
      }

      private void start()
      {
         synchronized (this)
         {
            if (original != null)
               throw new IllegalStateException("Update context has already been initialized");

            original = new CompletableFuture<>();
            try {
               // NOTE could block forever depending on supplier impl.
               original.complete(supplier.get());
            } catch (Exception ex) {
               original.completeExceptionally(ex);
            }
         }
      }

      private void finish()
      {
         // no-op -- reserved for future use
      }

      @Override
      public UUID getUpdateId()
      {
         return updateId;
      }

      @Override
      public String getId()
      {
         return entryId;
      }

      @Override
      public Instant getTimestamp()
      {
         if (timestamp == null)
            throw new IllegalStateException("This action has not been executed");

         return timestamp;
      }

      @Override
      public ActionType getActionType()
      {
         return action;
      }

      @Override
      public Account getActor()
      {
         return actor;
      }

      public void addError(String msg)
      {
         errors.add(msg);
      }

      public List<String> listErrors()
      {
         return Collections.unmodifiableList(errors);
      }

      public DTO getInitialState()
      {
         return initial;
      }

      @Override
      public DTO getOriginal()
      {
         // HACK wait arbitrary time to prevent waiting forever
         return getOriginal(1, TimeUnit.MINUTES);
      }

      public DTO getOriginal(long time, TimeUnit units)
      {
         synchronized (this)
         {
            if (original == null)
               throw new IllegalStateException(
                     "The original representation of this entry is not available. The update has not yet started.");
         }

         try
         {
            return original.get(time, units);
         }
         catch (InterruptedException e)
         {
            String ERR_CANCELLED = "Retreival of original entry {0} was cancelled";
            throw new RepositoryException(format(ERR_CANCELLED, entryId), e);
         }
         catch (TimeoutException e)
         {
            String ERR_TIMEOUT = "Failed to retrieve original entry {0} in a timely fashion";
            throw new RepositoryException(format(ERR_TIMEOUT, entryId), e);
         }
         catch (ExecutionException e)
         {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException)
               throw (RuntimeException)cause;
            if (cause instanceof Error)
               throw (Error)cause;

            String ERR_FAILED = "Failed to retrieve original entry {0}";
            throw new RepositoryException(format(ERR_FAILED, entryId), e);
         }
      }

      @Override
      public DTO getModified()
      {
         // HACK wait arbitrary time to prevent waiting forever
         return getModified(5, TimeUnit.MINUTES);
      }

      public DTO getModified(long time, TimeUnit units)
      {
         try
         {
            return modified.get(time, units);
         }
         catch (InterruptedException e)
         {
            String ERR_CANCELLED = "Retreival of modified entry {0} was cancelled";
            throw new RepositoryException(format(ERR_CANCELLED, entryId), e);
         }
         catch (TimeoutException e)
         {
            String ERR_TIMEOUT = "Failed to retrieve modified {0} in a timely fashion";
            throw new RepositoryException(format(ERR_TIMEOUT, entryId), e);
         }
         catch (ExecutionException e)
         {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException)
               throw (RuntimeException)cause;
            if (cause instanceof Error)
               throw (Error)cause;

            String ERR_FAILED = "Failed to obtain modified entry {0}";
            throw new RepositoryException(format(ERR_FAILED, entryId), e);
         }
      }
   }
}
