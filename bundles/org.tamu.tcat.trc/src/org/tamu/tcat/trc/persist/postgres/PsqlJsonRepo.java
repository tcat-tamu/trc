/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tamu.tcat.trc.persist.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.postgresql.util.PGobject;
import org.tamu.tcat.trc.persist.CommitHook;
import org.tamu.tcat.trc.persist.DocumentRepository;
import org.tamu.tcat.trc.persist.EditCommandFactory;
import org.tamu.tcat.trc.persist.RepositoryConfiguration;
import org.tamu.tcat.trc.persist.RepositoryException;
import org.tamu.tcat.trc.persist.RepositorySchema;
import org.tamu.tcat.trc.persist.postgres.id.DbBackedObfuscatingIdFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;

/**
 * A repository implementation intended to be registered as a service.
 */
public class PsqlJsonRepo<DTO, EditorType> implements DocumentRepository<DTO, DTO>
{
   private static final Logger logger = Logger.getLogger(PsqlJsonRepo.class.getName());

   private final String id;
   private final RepositorySchema schema;
   private final SqlExecutor exec;
   private final DbBackedObfuscatingIdFactory idFactory;
   private final Function<DTO, DTO> adapter;
   private final Class<DTO> recordType;

   private LoadingCache<String, DTO> cache;




   private String getRecordSql;
   private String createRecordSql;
   private String updateRecordSql;
   private String removeRecordSql;

   private EditCommandFactory<DTO, DTO, EditorType> editorFactory;



   public PsqlJsonRepo(RepositoryConfiguration<DTO, DTO, EditorType> config, SqlExecutor exec)
   {
      Objects.requireNonNull(exec, "SqlExecutor is null.");

      this.id = config.getId();
      this.adapter = config.getDataAdapter();
      this.schema = config.getSchema();
      this.editorFactory = config.getEditCommandFactory();
      this.recordType = config.getRecordType();
      this.exec = exec;

      idFactory = new DbBackedObfuscatingIdFactory();
      idFactory.setDatabaseExecutor(exec);
      idFactory.activate();

      // TODO initialize event notification tools

      this.getRecordSql = prepareGetSql(schema);
      this.createRecordSql = prepareInsertSql(schema);
      this.updateRecordSql = prepareUpdateSql(schema);
      this.removeRecordSql = prepareRemoveSql(schema);

      this.initCache();
   }

   private void initCache()
   {
      ObjectMapper mapper = new ObjectMapper();
      // TODO set config props; move to constructor
      cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, DTO>() {

               @Override
               public DTO load(String key) throws Exception
               {
                  String json = loadJson(key);
                  DTO model = mapper.readValue(json, recordType);
                  return model;
               }

            });
   }

   @Override
   public void close()
   {
      this.cache.invalidateAll();
      // TODO shut down notifications this.listeners.close();
   }

   // TODO: Need to check to see if each field contains information or not.
   private final static String GET_RECORD_SQL = "SELECT {0} FROM {1} WHERE {2} = ? {3}";
   private static String prepareGetSql(RepositorySchema defn)
   {
      String removedField = defn.getRemovedField();
      String isNotRemoved = (removedField != null)
                  ? MessageFormat.format("AND {0} IS NULL", removedField)
                  : "";

      return MessageFormat.format(GET_RECORD_SQL, defn.getDataField(), defn.getName(), isNotRemoved);
   }

   private final static String INSERT_SQL = "INSERT INTO {0} ({1}, {2}) VALUES(?, ?)";
   private static String prepareInsertSql(RepositorySchema defn)
   {
      return MessageFormat.format(INSERT_SQL, defn.getName(), defn.getDataField(), defn.getIdField());
   }

   private final static String UPDATE_SQL = "UPDATE {0} SET {1} = ?, {2} = now() WHERE {3} = ?";
   private static String prepareUpdateSql(RepositorySchema defn)
   {
      return MessageFormat.format(UPDATE_SQL, defn.getName(), defn.getDataField(), defn.getModifiedField(), defn.getIdField());
   }

   private final static String REMOVE_SQL =  "UPDATE {0} SET {1} = false, {2} = now() WHERE {3} = ?";
   private static String prepareRemoveSql(RepositorySchema defn)
   {
      return MessageFormat.format(REMOVE_SQL, defn.getName(), defn.getRemovedField(), defn.getModifiedField(), defn.getIdField());
   }

   public String getId()
   {
      return id;
   }

   @Override
   public Iterator<DTO> listAll() throws RepositoryException
   {
      throw new UnsupportedOperationException();
      // TODO Auto-generated method stub
   }

   @Override
   public DTO get(String id) throws RepositoryException
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
   public Collection<DTO> get(String... ids) throws RepositoryException
   {
      // HACK: this is potentially very inefficient. Should load records that are already in the
      //       cache and then execute a query that will load all remaining records from the DB
      //       in a single task (depending on the number of ids, possibly via multiple queries).

      HashMap<String, DTO> results = new HashMap<>();
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
   private String loadJson(String id) throws RepositoryException
   {
      Future<String> future = exec.submit((conn) -> {
         if (Thread.interrupted())
            throw new InterruptedException();

         return loadJson(conn, id);
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

            PGobject pgo = (PGobject)rs.getObject(schema.getName());
            return pgo.toString();
         }
      }
      catch (SQLException e)
      {
         throw new IllegalStateException("Faield to retrieve the record.", e);
      }
   }

   @Override
   public EditorType create()
   {
      return this.create(UUID.randomUUID().toString());
   }

   @Override
   public EditorType create(String id) throws UnsupportedOperationException
   {

      return this.editorFactory.create(id,  new CreateCommitHook());

   }

   @Override
   public EditorType edit(String id) throws RepositoryException
   {
      Supplier<DTO> s = () -> {
        try
        {
           return this.get(id);
        }
        catch (Exception e)
        {
           throw new IllegalStateException("Failed to retrieve reference state.");
        }
      };
      return this.editorFactory.edit(id, s , new CreateCommitHook());
   }


   @Override
   public Future<Boolean> delete(String personId)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   private class CreateCommitHook implements CommitHook<DTO>
   {

      @Override
      public Future<String> submit(DTO data, Object changeSet)
      {
         // TODO Auto-generated method stub
         return null;
      }

   }
}