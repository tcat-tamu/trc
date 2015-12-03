package edu.tamu.tcat.trc.repo.postgres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.RepositorySchema;
import edu.tamu.tcat.trc.repo.postgres.NotifyingTaskFactory.ObservableTask;

public class PsqlJacksonRepo<RecordType, DTO, EditCommandType> implements DocumentRepository<RecordType, EditCommandType>
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

   private NotifyingTaskFactory sqlTaskFactory;
   private LoadingCache<String, RecordType> cache;


   public PsqlJacksonRepo()
   {
   }

   public void setSqlExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void setIdFactory(Supplier<String> idFactory)
   {
      this.idFactory = idFactory;
   }

   public void setTableName(String tablename)
   {
      this.tablename = tablename;
   }


   public void setSchema(RepositorySchema schema)
   {
      this.schema = schema;
   }

   public void setCommandFactory(EditCommandFactory<DTO, EditCommandType> cmdFactory)
   {
      this.cmdFactory = cmdFactory;
   }

   public void setAdapter(Function<DTO, RecordType> adapter)
   {
      this.adapter = adapter;
   }


   public void setStorageType(Class<DTO> storageType)
   {
      this.storageType = storageType;
   }

   public void activate()
   {
      // TODO initialize event notification tools
      // default to UUID-based ids
      if (idFactory == null)
      {
         idFactory = () -> UUID.randomUUID().toString();
      }

      Objects.requireNonNull(exec, "The SQL executor has not bee supplied");
      Objects.requireNonNull(tablename, "The tablename has not bee supplied");
      if (tablename.trim().isEmpty())
         throw new IllegalStateException("The tablename must not be an empty string");

      Objects.requireNonNull(cmdFactory, "The edit command factory has not bee supplied");
      Objects.requireNonNull(schema, "The data schema has not bee supplied");
      Objects.requireNonNull(adapter, "The data adapter has not bee supplied");
      Objects.requireNonNull(storageType, "The storage type has not bee supplied");

      this.getRecordSql = prepareGetSql();
      this.createRecordSql = prepareInsertSql();
      this.updateRecordSql = prepareUpdateSql();
      this.removeRecordSql = prepareRemoveSql();

      this.sqlTaskFactory = new NotifyingTaskFactory();

      this.initCache();
   }

   @Override
   public void dispose()
   {
      this.cache.invalidateAll();
      this.cache = null;

      this.sqlTaskFactory.close();
      this.sqlTaskFactory = null;
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
      // TODO allow configuration to be specified;
      @Override
      public RecordType load(String key) throws Exception
      {
         DTO dto = loadStoredRecord(key);
         RecordType record = adapter.apply(dto);
         return record;
      }
   }

   private String prepareGetSql()
   {
      String GET_RECORD_SQL = "SELECT {0} FROM {1} WHERE {2} = ? {3}";

      String removedField = schema.getRemovedField();
      String isNotRemoved = (removedField != null)
                  ? MessageFormat.format("AND {0} IS NULL", removedField)
                  : "";

      return MessageFormat.format(GET_RECORD_SQL, schema.getDataField(), tablename, schema.getIdField(), isNotRemoved);
   }

   private String prepareInsertSql()
   {
      String INSERT_SQL = "INSERT INTO {0} ({1}, {2}) VALUES(?, ?)";

      return MessageFormat.format(INSERT_SQL, tablename, schema.getDataField(), schema.getIdField());
   }

   private String prepareUpdateSql()
   {
      String UPDATE_SQL = "UPDATE {0} SET {1} = ?{2} WHERE {3} = ?";

      String idCol = schema.getIdField();
      String dataCol = schema.getDataField();
      String modifiedCol = schema.getModifiedField();
      String dateModClause = hasDateModifiedField() ? ", " + modifiedCol + " = now()": "";
      return MessageFormat.format(UPDATE_SQL, tablename, dataCol, dateModClause, idCol);
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
         return MessageFormat.format(MARK_REMOVED_SQL, tablename, removedField, dateModClause, idField);
      }
      else
      {
         return MessageFormat.format(DELETE_SQL, tablename, idField);
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
      return new PagedRecordIterator<RecordType>(this::getPersonBlock, this::parse, 100);
   }

   public RecordType parse(String json)
   {
      try
      {
         ObjectMapper mapper = getObjectMapper();

         DTO dto = mapper.readValue(json, storageType);
         return adapter.apply(dto);
      }
      catch (IOException ex)
      {
         String message = MessageFormat.format("Failed to parse stored record data.\n\t{0}", json);
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
      String sql = MessageFormat.format(template, schema.getDataField(), tablename,
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

   @Override
   public RecordType get(String id) throws RepositoryException
   {
      try
      {
         return cache.get(id);
      }
      catch (ExecutionException ex)
      {
         Throwable cause = ex.getCause();
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

      return Futures.get(future, RepositoryException.class);
   }

   /**
    * Called from within the database executor to retrieve the underlying JSON representation
    * of an item.
    *
    * @param conn
    * @param id
    * @return
    * @throws RepositoryException
    * @throws InterruptedException
    */
   private String loadJson(Connection conn, String id) throws RepositoryException
   {
      try (PreparedStatement ps = conn.prepareStatement(getRecordSql))
      {
         ps.setString(1, id);
         try (ResultSet rs = ps.executeQuery())
         {
            if (!rs.next())
               throw new RepositoryException("Could not find record for id = '" + id + "'");

            PGobject pgo = (PGobject)rs.getObject(schema.getDataField());
            return pgo.toString();
         }
      }
      catch (SQLException e)
      {
         throw new IllegalStateException("Faield to retrieve the record.", e);
      }
   }

   @Override
   public EditCommandType create()
   {
      return create(idFactory.get());
   }

   @Override
   public EditCommandType create(String id) throws UnsupportedOperationException
   {
      return this.cmdFactory.edit(id,
            () -> null,
            (dto, changeSet) -> doCreate(id, dto, changeSet));
   }

   @Override
   public EditCommandType edit(String id) throws RepositoryException
   {
      return this.cmdFactory.edit(id,
            () -> loadStoredRecordUnchecked(id),
            (dto, changeSet) -> doEdit(id, dto, changeSet));
   }

   @Override
   public Future<Boolean> delete(String id) throws UnsupportedOperationException
   {
      ObservableTask<Boolean> task = sqlTaskFactory.wrap((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(removeRecordSql))
         {
            ps.setString(1, id);
            int ct = ps.executeUpdate();
            return Boolean.valueOf(ct == 1);
         }
         catch (SQLException e)
         {
            String message = MessageFormat.format("Failed to mark record as deleted. Record id: {0}", id);
            throw new IllegalStateException(message, e);
         }
      });

      task.afterExecution(recordId -> cache.invalidate(recordId));

      // TODO fire notification
      return exec.submit(task);
   }

   private DTO loadStoredRecordUnchecked(String id)
   {
      // TODO (REVIEW) is this a good idea or should we throw an unchecked exception.
      //      Need to document decision on the commit hook
      try
      {
         return loadStoredRecord(id);
      }
      catch (RepositoryException ex)
      {
         String msg = MessageFormat.format("Failed to load stored record for {0}", id);
         logger.log(Level.WARNING, msg, ex);
         return null;
      }
   }

   private Future<String> doCreate(String id, DTO record, Object changeSet)
   {

      ObservableTask<String> task = sqlTaskFactory.wrap((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(createRecordSql))
         {
            ObjectMapper mapper = getObjectMapper();

            PGobject json = new PGobject();
            json.setType("json");
            json.setValue(mapper.writeValueAsString(record));

            ps.setObject(1, json);
            ps.setString(2, id);

            int ct = ps.executeUpdate();
            if (ct != 1)
               throw new IllegalStateException("Failed to create record for id [" + id + "]. Unexpected number of rows updates [" + ct + "]");
         }
         catch (IOException e)
         {
            // NOTE this is an internal configuration error. The JsonMapper should be configured to
            //      serialize HistoricalFigureDV instances correctly.
            throw new IllegalArgumentException("Failed to serialize the supplied record [" + record + "]", e);
         }

         return id;
      });

      // We must invalidate the cache after execution has completed to ensure that a call
      // to #get() will not reload the stale data after the cache has been invalidated but
      // before the DB task executes. This would result in persistently stale data loaded into
      // the cache.
      //
      // Note that this allows clients to see stale data if they call get after the DB
      // changes but before the cache is invalidated.
      //
      // Note that a call to #get following a call to create or edit is not guaranteed to
      // see the results of the record update.
      task.afterExecution(recordId -> cache.invalidate(recordId));
//    TODO add notifications

//      task.afterExecution(id -> notifyPersonUpdate(UpdateEvent.UpdateAction.UPDATE, id));

      return exec.submit(task);
   }

   private Future<String> doEdit(String id, DTO record, Object changeSet)
   {
      ObservableTask<String> task = sqlTaskFactory.wrap((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(updateRecordSql))
         {
            ObjectMapper mapper = getObjectMapper();

            PGobject json = new PGobject();
            json.setType("json");
            json.setValue(mapper.writeValueAsString(record));

            ps.setObject(1, json);
            ps.setString(2, id);

            int ct = ps.executeUpdate();
            if (ct != 1)
               throw new IllegalStateException("Failed to update record for id [" + id + "]. Unexpected number of rows updates [" + ct + "]");
         }
         catch (IOException e)
         {
            throw new IllegalArgumentException("Failed to serialize the supplied record [" + record + "]", e);
         }

         return id;
      });

      task.afterExecution(recordId -> cache.invalidate(recordId));
//      TODO add notifications
//      task.afterExecution(recordId -> notifyPersonUpdate(UpdateEvent.UpdateAction.UPDATE, id));

      return exec.submit(task);
   }

   private ObjectMapper getObjectMapper()
   {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      return mapper;
   }
}
