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
package edu.tamu.tcat.trc.entries.types.bio.postgres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.core.IdFactory;
import edu.tamu.tcat.trc.entries.notification.BaseUpdateEvent;
import edu.tamu.tcat.trc.entries.notification.EntryUpdateHelper;
import edu.tamu.tcat.trc.entries.notification.NotifyingTaskFactory;
import edu.tamu.tcat.trc.entries.notification.NotifyingTaskFactory.ObservableTask;
import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.entries.repo.ExecutionFailedException;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.dto.PersonDTO;
import edu.tamu.tcat.trc.entries.types.bio.postgres.model.PersonImpl;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditPersonCommand;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;
import edu.tamu.tcat.trc.entries.types.bio.repo.PersonChangeEvent;
import edu.tamu.tcat.trc.entries.types.bio.repo.PersonNotAvailableException;

/**
 * A repository implementation intended to be registered as a service.
 */
public class PsqlPeopleRepo implements PeopleRepository
{
   private static final Logger logger = Logger.getLogger(PsqlPeopleRepo.class.getName());

   private final static String GET_PERSON_SQL = "SELECT historical_figure FROM people WHERE id = ?";
   private final static String GET_ALL_SQL = "SELECT historical_figure FROM people WHERE active = true";
   private static final String INSERT_SQL = "INSERT INTO people (historical_figure, id) VALUES(?, ?)";
   private static final String UPDATE_SQL = "UPDATE people SET historical_figure = ?, modified = now() WHERE id = ?";
   private final static String DELETE_SQL =  "UPDATE people SET active = false, modified = now() WHERE id = ?";

   //HACK: doesn't really matter for now, but once authz is in place, this will be the user's id
   private static final UUID ACCOUNT_ID_REPO = UUID.randomUUID();

   public static final String ID_CONTEXT = "people";
   private SqlExecutor exec;
   private IdFactory idFactory;
   private ObjectMapper mapper;

   // TODO replace with notifications helper
   private EntryUpdateHelper<PersonChangeEvent> listeners;
   private NotifyingTaskFactory sqlTaskFactory;

   private LoadingCache<String, Person> cache;

   public PsqlPeopleRepo()
   {
   }

   // Dependency Inject
   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   // Dependency Inject
   public void setIdFactory(IdFactory factory)
   {
      this.idFactory = factory;
   }

   // DS entry point
   public void activate()
   {
      Objects.requireNonNull(exec);
      Objects.requireNonNull(idFactory);

      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      sqlTaskFactory = new NotifyingTaskFactory();
      listeners = new EntryUpdateHelper<>();

      initCache();
   }

   private void initCache()
   {
      cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Person>() {

               @Override
               public Person load(String key) throws Exception
               {
                  String json = loadJson(key);
                  PersonDTO dto = parseJson(json, key);
                  return new PersonImpl(dto);
               }

            });
   }

   // DS disposal
   public void dispose()
   {
      this.mapper = null;
      this.exec = null;
      this.idFactory = null;

      this.cache.invalidateAll();
      this.listeners.close();
   }

   @Override
   public Person get(String personId) throws NoSuchCatalogRecordException
   {
      try
      {
         return cache.get(personId);
      }
      catch (ExecutionException ex)
      {
         Throwable cause = ex.getCause();
         if (cause instanceof NoSuchCatalogRecordException)
            throw (NoSuchCatalogRecordException)cause;
         if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;

         throw new IllegalStateException("Failed to retrieve biographical record [" + personId + "]", cause);
      }
   }

   /**
    * @return the JSON representation associated with this id.
    */
   private String loadJson(String personId) throws NoSuchCatalogRecordException
   {
      Future<String> future = exec.submit((conn) -> {
         if (Thread.interrupted())
            throw new InterruptedException();

         try (PreparedStatement ps = conn.prepareStatement(GET_PERSON_SQL))
         {
            ps.setString(1, personId);
            try (ResultSet rs = ps.executeQuery())
            {
               if (!rs.next())
                  throw new NoSuchCatalogRecordException("Could not find record for person [" + personId + "]");

               PGobject pgo = (PGobject)rs.getObject("historical_figure");
               return pgo.toString();
            }
         }
         catch (SQLException e)
         {
            throw new IllegalStateException("Faield to retrieve person.", e);
         }
      });

      return Futures.get(future, NoSuchCatalogRecordException.class);
   }

   @Override
   public Iterator<Person> listAll() throws CatalogRepoException
   {
      return new PagedItemIterator<Person>(this::getPersonBlock, this::parse, 100);
   }


   private Future<List<String>> getPersonBlock(int offset, int limit)
   {
      String sqlGetBlock = "SELECT historical_figure "
                              + "FROM people "
                              + "ORDER BY id "
                              + "LIMIT {0} OFFSET {1}";

      return exec.submit((conn) -> getPageBlock(conn, sqlGetBlock, offset, limit));
   }

   private List<String> getPageBlock(Connection conn, String query, int offset, int limit) throws InterruptedException
   {
      if (Thread.interrupted())
         throw new InterruptedException();

      String SQL = MessageFormat.format(query, Integer.toString(limit), Integer.toString(offset));
      List<String> jsonData = new ArrayList<>();
      try (Statement stmt = conn.createStatement())
      {
         ResultSet rs = stmt.executeQuery(SQL);
         while (rs.next())
         {
            if (Thread.interrupted())
               throw new InterruptedException();

            PGobject pgo = (PGobject)rs.getObject("historical_figure");
            jsonData.add(pgo.toString());
         }

         return jsonData;
      }
      catch (SQLException e)
      {
         throw new IllegalStateException("Faield to retrieve person.", e);
      }
   }

   @Override
   public EditPersonCommand create()
   {
      String id = idFactory.getNextId(ID_CONTEXT);
      return create(id);
   }

   @Override
   public EditPersonCommand create(String id)
   {
      PersonDTO dto = new PersonDTO();
      dto.id = id;

      EditPeopleCommandImpl command = new EditPeopleCommandImpl(dto);
      command.setCommitHook(this::create);
      return command;
   }

   public Future<String> create(final PersonDTO histFigure)
   {
      ObservableTask<String> task = sqlTaskFactory.wrap((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL))
         {
            PGobject json = new PGobject();
            json.setType("json");
            json.setValue(mapper.writeValueAsString(histFigure));

            ps.setObject(1, json);
            ps.setString(2, histFigure.id);

            int ct = ps.executeUpdate();
            if (ct != 1)
               throw new ExecutionFailedException("Failed to create historical figure. Unexpected number of rows updates [" + histFigure.id + "]");
         }
         catch (IOException e)
         {
            // NOTE this is an internal configuration error. The JsonMapper should be configured to
            //      serialize HistoricalFigureDV instances correctly.
            throw new ExecutionFailedException("Failed to serialize the supplied historical figure [" + histFigure.id + "]", e);
         }
         catch (SQLException sqle)
         {
            throw new ExecutionFailedException("Failed to save historical figure [" + histFigure.id+ "]", sqle);
         }

         return histFigure.id;
      });

      task.afterExecution(id -> notifyPersonUpdate(UpdateEvent.UpdateAction.UPDATE, id));
      task.afterExecution(id -> cache.invalidate(id));

      return exec.submit(task);
   }

   @Override
   public EditPersonCommand update(String personId) throws NoSuchCatalogRecordException
   {
      PersonDTO dto = parseJson(loadJson(personId), personId);
      EditPeopleCommandImpl command = new EditPeopleCommandImpl(dto);

      command.setCommitHook(this::update);
      return command;
   }

   public Future<String> update(final PersonDTO histFigure)
   {
      ObservableTask<String> task = sqlTaskFactory.wrap((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL))
         {
            PGobject json = new PGobject();
            json.setType("json");
            json.setValue(mapper.writeValueAsString(histFigure));

            ps.setObject(1, json);
            ps.setString(2, histFigure.id);

            int ct = ps.executeUpdate();
            if (ct != 1)
               throw new IllegalStateException("Failed to create historical figure. Unexpected number of rows updates [" + ct + "]");
         }
         catch (IOException e)
         {
            throw new IllegalArgumentException("Failed to serialize the supplied historical figure [" + histFigure + "]", e);
         }

         return histFigure.id;
      });

      task.afterExecution(id -> notifyPersonUpdate(UpdateEvent.UpdateAction.UPDATE, id));
      task.afterExecution(id -> cache.invalidate(id));

      return exec.submit(task);
   }

   @Override
   public Future<Boolean> delete(final String personId) throws NoSuchCatalogRecordException
   {
      ObservableTask<Boolean> task = sqlTaskFactory.wrap((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL))
         {
            ps.setString(1, personId);
            int ct = ps.executeUpdate();
            return Boolean.valueOf(ct == 1);
         }
         catch (SQLException e)
         {
            throw new IllegalStateException("Faield to de-activate personId:" + personId, e);
         }
      });

      task.afterExecution(id -> notifyPersonUpdate(UpdateEvent.UpdateAction.DELETE, personId));
      task.afterExecution(id -> cache.invalidate(id));

      return exec.submit(task);
   }

   private void notifyPersonUpdate(UpdateEvent.UpdateAction type, String id)
   {
      PeopleChangeEventImpl evt = new PeopleChangeEventImpl(type, id);
      listeners.after(evt);
   }

   @Override
   public AutoCloseable addUpdateListener(UpdateListener<PersonChangeEvent> ears)
   {
      return listeners.register(ears);
   }

   private class PeopleChangeEventImpl extends BaseUpdateEvent implements PersonChangeEvent
   {
      public PeopleChangeEventImpl(UpdateEvent.UpdateAction type, String id)
      {
         super(id, type, ACCOUNT_ID_REPO, Instant.now());
      }

      @Override
      public Person getPerson() throws PersonNotAvailableException
      {
         try
         {
            return get(id);
         }
         catch (Exception e)
         {
            throw new PersonNotAvailableException("Failed to retrieve person [" + id + "]", e);
         }
      }
   }

   private Person parse(String json)
   {
      try
      {
         PersonDTO dto = mapper.readValue(json, PersonDTO.class);
         return new PersonImpl(dto);
      }
      catch (IOException je)
      {
         throw new IllegalStateException("Failed to parse JSON record for person", je);
      }
   }
   private PersonDTO parseJson(String json, String id)
   {
      try
      {
         return mapper.readValue(json, PersonDTO.class);
      }
      catch (IOException je)
      {
         throw new IllegalStateException("Failed to parse JSON record for person [id = " + id + "]", je);
      }
   }
}