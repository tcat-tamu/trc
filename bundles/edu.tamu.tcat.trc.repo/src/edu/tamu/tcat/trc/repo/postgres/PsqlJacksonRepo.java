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
import java.util.Optional;
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
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.UpdateContext;
import edu.tamu.tcat.trc.repo.UpdateContext.ActionType;

public class PsqlJacksonRepo<RecordType, DTO, EditCommandType> implements DocumentRepository<RecordType, DTO, EditCommandType>
{
   public static final String DATA = "data";

   private static final String GET_RECORD_SQL = "SELECT data FROM {0} WHERE id = ? AND removed IS NULL";
   private static final String INSERT_SQL = "INSERT INTO {0} (data, id) VALUES(?, ?)";
   private static final String UPDATE_SQL = "UPDATE {0} SET data = ?, last_modified = now() WHERE id = ?";
   private static final String MARK_REMOVED_SQL =  "UPDATE {0} SET removed = now(), last_modified = now() WHERE id = ?";
//   private static final String DELETE_SQL =  "DELETE FROM {0} WHERE id = ?";
   private static final String EXISTS_SQL = "SELECT id FROM {0} WHERE id = ? AND removed IS NULL";

   private static final Logger logger = Logger.getLogger(PsqlJacksonRepo.class.getName());

   private SqlExecutor exec;
   private Supplier<String> idFactory;

   private String tablename;
   private Function<DTO, RecordType> adapter;
   private Class<DTO> storageType;

   private EditCommandFactory<DTO, EditCommandType> cmdFactory;

   private String getRecordSql;
   private String createRecordSql;
   private String updateRecordSql;
   private String removeRecordSql;

   private final Map<UUID, EntryUpdateObserver<DTO>> preCommitTasks = new ConcurrentHashMap<>();
   private final Map<UUID, EntryUpdateObserver<DTO>> postCommitTasks = new ConcurrentHashMap<>();

   private LoadingCache<String, Optional<RecordType>> cache;

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
      Objects.requireNonNull(adapter, "The data adapter has not bee supplied");
      Objects.requireNonNull(storageType, "The storage type has not bee supplied");

      logger.info(format("Initializing document repository using table {0}", tablename));

      this.getRecordSql = format(GET_RECORD_SQL, tablename);
      this.createRecordSql = format(INSERT_SQL, tablename);
      this.updateRecordSql = format(UPDATE_SQL, tablename);
      this.removeRecordSql = format(MARK_REMOVED_SQL, tablename);

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

   private class CacheLoaderImpl extends CacheLoader<String, Optional<RecordType>>
   {
      @Override
      public Optional<RecordType> load(String key) throws Exception
      {
         Optional<DTO> dto = loadStoredRecord(key);
         Optional<RecordType> record = dto.map(adapter::apply);
         return record;
      }
   }

   public String buildNotRemovedClause()
   {
      return "AND removed IS NULL";
   }




   @Override
   public Iterator<RecordType> listAll()
   {
      return new PagedRecordIterator<>(this::getPageBlock, json -> adapter.apply(parse(json)), 100);
   }

   public boolean exists(String id)
   {
      return unwrap(exec.submit(conn -> exists(conn, id)),
            () -> format("Failed to determine if entry [{0}] exists.", id));
   }

   @Override
   public Optional<RecordType> get(String id)
   {
      try
      {
         return cache.get(id);
      }
      catch (ExecutionException | UncheckedExecutionException ex)
      {
         throw new RepositoryException("Failed to retrieve record [" + id + "]", ex.getCause());
      }
   }

   @Override
   public Collection<RecordType> get(String... ids) throws RepositoryException
   {
      // TODO: this is very inefficient. Should load records that are already in the
      //       cache and then execute a query that will load all remaining records from the DB
      //       in a single task (depending on the number of ids, possibly via multiple queries).

      HashMap<String, RecordType> results = new HashMap<>();
      for (String id: ids)
      {
         if (results.containsKey(id))
            continue;

         get(id).ifPresent(record -> results.put(id, record));
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
      UpdateContextImpl context = new UpdateContextImpl(id, ActionType.CREATE, account, () -> Optional.empty());
      UpdateStrategyImpl updater = new UpdateStrategyImpl(context, dto -> doCreate(id, dto));

      return this.cmdFactory.create(id, updater);
   }

   @Override
   public EditCommandType edit(Account account, String id) throws RepositoryException
   {
      UpdateContextImpl context = new UpdateContextImpl(id, ActionType.EDIT, account, () -> loadStoredRecord(id));
      UpdateStrategyImpl updater = new UpdateStrategyImpl(context, dto -> doEdit(id, dto));

      return this.cmdFactory.edit(id, updater);
   }

   @Override
   public CompletableFuture<Boolean> delete(Account account, String id)
   {
      // FIXME this looks like a mess
      UpdateContextImpl context = new UpdateContextImpl(id, ActionType.REMOVE, account, () -> {
         try {
            return loadStoredRecord(id);
         } catch (Exception e) {
            String string = "Internal error trying to restore document {0} from repo {1}";
            logger.log(Level.SEVERE, format(string, id, this.tablename), e);
            return Optional.empty();
         }
      });

      // HACK it would be nice if we could directly return the result of doDelete,
      //      UpdateStrategy would then have to support a custom result type
      CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();
      UpdateStrategyImpl updater = new UpdateStrategyImpl(context,
            dto -> {
               return doDelete(id).exceptionally(ex -> {
                  resultFuture.completeExceptionally(ex);
                  return false;
               }).thenApply(result -> {
                  if (!resultFuture.isDone())
                     resultFuture.complete(result);
                  return null;   // the object has been removed
               });
            });


      updater.update(ctx -> null);

      return resultFuture;
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

   private Future<List<String>> getPageBlock(int offset, int limit)
   {

      return exec.submit((conn) -> getPageBlock(conn, offset, limit));
   }

   private List<String> getPageBlock(Connection conn, int offset, int limit) throws InterruptedException
   {
      if (Thread.interrupted())
         throw new InterruptedException();

      String template = "SELECT data FROM {0} {1} ORDER BY id LIMIT {2} OFFSET {3}";
      String filterRemoved = "WHERE removed IS NULL";
      String sql = format(template,
            tablename,
            filterRemoved,
            Integer.toString(limit),
            Integer.toString(offset));

      List<String> jsonData = new ArrayList<>();
      try (Statement stmt = conn.createStatement())
      {
         ResultSet rs = stmt.executeQuery(sql);
         while (rs.next())
         {
            if (Thread.interrupted())
               throw new InterruptedException();

            PGobject pgo = (PGobject)rs.getObject(DATA);
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
   private Optional<DTO> loadStoredRecord(String id) throws RepositoryException
   {
      Future<Optional<String>> future = exec.submit((conn) -> loadJson(conn, id));

      Optional<String> json = unwrap(future, () -> format("Failed to load DTO for entry with id={0}", id));
      return json.map(this::parse);
   }

   private DTO parse(String json)
   {
      try
      {
         ObjectMapper mapper = getObjectMapper();

         return mapper.readValue(json, storageType);
      }
      catch (IOException ex)
      {
         String message = format("Failed to parse stored record data.\n\t{0}", json);
         throw new IllegalStateException(message, ex);
      }
   }

   private boolean exists(Connection conn, String id) throws RepositoryException
   {
      String sql = format(EXISTS_SQL, tablename);

      try (PreparedStatement ps = conn.prepareStatement(sql))
      {
         ps.setString(1, id);
         ResultSet rs = ps.executeQuery();
         return rs.next();
      }
      catch (SQLException e)
      {
         throw new RepositoryException("Failed to check existance the record.", e);
      }
   }

   /**
    * Called from within the database executor to retrieve the underlying JSON representation
    * of an item.
    *
    * @param conn
    * @param id
    * @return
    *
    * @throws RepositoryException If an unknown internal error occurred.
    * @throws InterruptedException If the execution was interrupted
    */
   private Optional<String> loadJson(Connection conn, String id)
         throws InterruptedException, RepositoryException
   {
      if (Thread.interrupted())
         throw new InterruptedException();

      try (PreparedStatement ps = conn.prepareStatement(getRecordSql))
      {
         ps.setString(1, id);
         try (ResultSet rs = ps.executeQuery())
         {
            if (!rs.next())
               return Optional.empty();

            PGobject pgo = (PGobject)rs.getObject(DATA);
            return Optional.of(pgo.toString());
         }
      }
      catch (SQLException e)
      {
         throw new RepositoryException("Failed to retrieve the record.", e);
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

   private CompletableFuture<Boolean> doDelete(String id)
   {
      return exec.submit((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(removeRecordSql))
         {
            ps.setString(1, id);
            int ct = ps.executeUpdate();
            cache.invalidate(id);
            return Boolean.valueOf(ct == 1);
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
            String template = "Post-commit task failed following update of entry {0}. Update id: {1}\n\tReason: {2}";
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
      private final Supplier<Optional<DTO>> supplier;

      private DTO initial;
      private CompletableFuture<DTO> original;
      private CompletableFuture<DTO> modified = new CompletableFuture<>();

      private final List<String> errors = new CopyOnWriteArrayList<>();

      UpdateContextImpl(String id, ActionType action, Account actor, Supplier<Optional<DTO>> supplier)
      {
         this.entryId = id;
         this.action = action;
         this.actor = actor;
         this.supplier = supplier;

         this.initial = supplier.get().orElse(null);
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
               Optional<DTO> orig = supplier.get();
               original.complete(orig.orElse(null));
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

      @Override
      public void addError(String msg)
      {
         errors.add(msg);
      }

      @Override
      public List<String> listErrors()
      {
         return Collections.unmodifiableList(errors);
      }

      @Override
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
