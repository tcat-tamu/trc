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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Logger;

import org.tamu.tcat.trc.persist.DocumentRepository;
import org.tamu.tcat.trc.persist.RecordEditCommand;
import org.tamu.tcat.trc.persist.RepositoryDataStore.RepositoryConfiguration;
import org.tamu.tcat.trc.persist.RepositoryException;
import org.tamu.tcat.trc.persist.RepositorySchema;
import org.tamu.tcat.trc.persist.postgres.id.DbBackedObfuscatingIdFactory;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;

/**
 * A repository implementation intended to be registered as a service.
 */
public class PsqlJsonRepo<T> implements DocumentRepository<T>
{
   private static final Logger logger = Logger.getLogger(PsqlJsonRepo.class.getName());

   private final String id;
   private final RepositorySchema schema;
   private final SqlExecutor exec;
   private final DbBackedObfuscatingIdFactory idFactory;
   private final Function<String, T> adapter;

   private final static String GET_RECORD_SQL = "SELECT {0} FROM {1} WHERE {2} = ?";


   public PsqlJsonRepo(RepositoryConfiguration<String, T> config, SqlExecutor exec)
   {
      this.id = config.getId();
      this.adapter = config.getDataAdapter();
      this.schema = config.getSchema();
      this.exec = exec;

      idFactory = new DbBackedObfuscatingIdFactory();
      idFactory.setDatabaseExecutor(exec);
      idFactory.activate();

      this.initSql(schema);
   }

   private void initSql(RepositorySchema defn)
   {
      String getSql = prepareGetSql(defn);
   }

   private String prepareGetSql(RepositorySchema defn)
   {
      return MessageFormat.format(GET_RECORD_SQL, defn.getDataField());
   }

   public String getId()
   {
      return id;
   }

   @Override
   public Iterator<T> listAll() throws RepositoryException
   {
      throw new UnsupportedOperationException();
      // TODO Auto-generated method stub
   }

   @Override
   public T get(String id) throws RepositoryException
   {
      throw new UnsupportedOperationException();
      // TODO Auto-generated method stub
   }

   @Override
   public RecordEditCommand<T> create(String id) throws UnsupportedOperationException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Collection<T> get(String... ids) throws RepositoryException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public RecordEditCommand<T> create()
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   @Override
   public RecordEditCommand<T> edit(String id) throws RepositoryException
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   @Override
   public Future<Boolean> delete(String personId)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }
}