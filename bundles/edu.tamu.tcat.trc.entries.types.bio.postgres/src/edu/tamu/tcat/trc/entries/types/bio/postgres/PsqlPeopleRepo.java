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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.core.IdFactory;
import edu.tamu.tcat.trc.entries.notification.BaseUpdateEvent;
import edu.tamu.tcat.trc.entries.notification.DataUpdateObserverAdapter;
import edu.tamu.tcat.trc.entries.notification.ObservableTaskWrapper;
import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.entries.repo.ExecutionFailedException;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.entries.types.bio.dto.PersonDTO;
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

   private static final String ID_CONTEXT = "people";
   private SqlExecutor exec;
   private IdFactory idFactory;
   private ObjectMapper mapper;

   private ExecutorService notifications;
   private final CopyOnWriteArrayList<Consumer<PersonChangeEvent>> listeners = new CopyOnWriteArrayList<>();

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

      notifications = Executors.newCachedThreadPool();
   }

   // DS disposal
   public void dispose()
   {
      this.mapper = null;
      this.exec = null;
      this.idFactory = null;
      shutdownNotificationsExec();
   }

   private void shutdownNotificationsExec()
   {
      try
      {
         notifications.shutdown();
         notifications.awaitTermination(10, TimeUnit.SECONDS);    // HACK: make this configurable
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to shut down event notifications executor in a timely fashion.", ex);
         try {
            List<Runnable> pendingTasks = notifications.shutdownNow();
            logger.info("Forcibly shutdown notifications executor. [" + pendingTasks.size() + "] pending tasks were aborted.");
         } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred attempting to forcibly shutdown executor service", e);
         }
      }
   }


   @Override
   public Iterable<Person> findPeople() throws CatalogRepoException
   {
      Future<List<Person>> future = exec.submit((conn) -> {
         List<Person> people = new ArrayList<Person>();
         try (PreparedStatement ps = conn.prepareStatement(GET_ALL_SQL);
              ResultSet rs = ps.executeQuery())
         {

            while (rs.next())
            {
               PGobject pgo = (PGobject)rs.getObject("historical_figure");
               people.add(parseJson(pgo.toString(), mapper));
            }
         }

         return people;
      });

      try
      {
         return future.get();
      }
      catch (Exception e)
      {
         throw new CatalogRepoException("Failed to retrieve people", e);
      }
   }

   @Override
   public Iterable<Person> findByName(String prefix) throws CatalogRepoException
   {
      List<Person> results = new ArrayList<>();
      prefix = prefix.toLowerCase();

      Iterable<Person> people = findPeople();
      for (Person p : people)
      {
         if (p.getCanonicalName().getFamilyName().toLowerCase().startsWith(prefix)) {
            results.add(p);
            continue;
         }

         for (PersonName name : p.getAlternativeNames())
         {
            String fname = name.getFamilyName();
            if (fname != null && fname.toLowerCase().startsWith(prefix))
            {
               results.add(p);
               break;
            }
         }
      }

      return results;
   }

   @Override
   public Person get(String personId) throws NoSuchCatalogRecordException
   {
      Future<Person> future = exec.submit((conn) -> {
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
               return parseJson(pgo.toString(), mapper);
            }
         }
         catch (SQLException e)
         {
            throw new IllegalStateException("Faield to retrieve person.", e);
         }
      });

      try
      {
         return future.get();
      }
      catch (ExecutionException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof NoSuchCatalogRecordException)
            throw (NoSuchCatalogRecordException)cause;
         if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;

         throw new IllegalStateException("Unexpected problems while attempting to retrieve biographical record [" + personId + "]", e);
      }
      catch (InterruptedException e) {
         throw new IllegalStateException("Failed to retrieve biographical record [" + personId + "]", e);
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
      command.setCommitHook((p) -> {
         PeopleChangeNotifier createdNotifier = new PeopleChangeNotifier(UpdateEvent.UpdateAction.CREATE);
         ObservableTaskWrapper<String> wrappedTask = new ObservableTaskWrapper<String>(makeCreateTask(p), createdNotifier);

         return exec.submit(wrappedTask);
      });

      return command;
   }

   @Override
   public EditPersonCommand update(String personId) throws NoSuchCatalogRecordException
   {
      PersonDTO dto = PersonDTO.create(get(personId));
      EditPeopleCommandImpl command = new EditPeopleCommandImpl(dto);
      command.setCommitHook((p) -> {
         PeopleChangeNotifier modifiedNotifier = new PeopleChangeNotifier(UpdateEvent.UpdateAction.UPDATE);
         ObservableTaskWrapper<String> wrappedTask = new ObservableTaskWrapper<String>(makeUpdateTask(p), modifiedNotifier);

         return exec.submit(wrappedTask);
      });


      return command;
   }

   @Override
   public EditPersonCommand delete(final String personId) throws NoSuchCatalogRecordException
   {
      PersonDTO dto = PersonDTO.create(get(personId));
      EditPeopleCommandImpl command = new EditPeopleCommandImpl(dto);
      command.setCommitHook((p) -> {
         PeopleChangeNotifier deletedNotifier = new PeopleChangeNotifier(UpdateEvent.UpdateAction.DELETE);
         ObservableTaskWrapper<String> wrappedTask = new ObservableTaskWrapper<String>(makeDeleteTask(personId), deletedNotifier);

         return exec.submit(wrappedTask);
      });


      return command;
   }

   private SqlExecutor.ExecutorTask<String> makeDeleteTask(final String personId)
   {
      return (conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL))
         {
            ps.setString(1, personId);

            int ct = ps.executeUpdate();
            if (ct != 1)
               throw new IllegalStateException("Failed to de-activate historical figure. Unexpected number of rows updates [" + ct + "]");
         }
         catch (SQLException e)
         {
            throw new IllegalStateException("Faield to de-activate personId:" + personId, e);
         }

         return personId;
      };
   }

   private SqlExecutor.ExecutorTask<String> makeCreateTask(final PersonDTO histFigure)
   {
      String id = histFigure.id;
      return (conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL))
         {
            PGobject jsonObject = toPGobject(histFigure);

            ps.setObject(1, jsonObject);
            ps.setString(2, id);

            int ct = ps.executeUpdate();
            if (ct != 1)
               throw new ExecutionFailedException("Failed to create historical figure. Unexpected number of rows updates [" + ct + "]");
         }
         catch (IOException e)
         {
            // NOTE this is an internal configuration error. The JsonMapper should be configured to
            //      serialize HistoricalFigureDV instances correctly.
            throw new ExecutionFailedException("Failed to serialize the supplied historical figure [" + id + "]", e);
         }
         catch (SQLException sqle)
         {
            throw new ExecutionFailedException("Failed to save historical figure [" + id + "]", sqle);
         }

         return histFigure.id;
      };
   }

   private SqlExecutor.ExecutorTask<String> makeUpdateTask(final PersonDTO histFigure)
   {
      return (conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL))
         {
            PGobject json = toPGobject(histFigure);
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
      };
   }


   private PGobject toPGobject(PersonDTO dto) throws SQLException, IOException
   {
      PGobject jsonObject = new PGobject();
      jsonObject.setType("json");
      jsonObject.setValue(mapper.writeValueAsString(dto));

      return jsonObject;
   }

   private static Person parseJson(String json, ObjectMapper mapper)
   {
      try
      {
         PersonDTO dv = mapper.readValue(json, PersonDTO.class);
         return PersonDTO.instantiate(dv);
      }
      catch (IOException je)
      {
         // NOTE: possible data leak. If this exception is propagated to someone who isn't authorized to see this record...
         throw new IllegalStateException("Cannot parse person from JSON:\n" + json, je);
      }
   }

   private void notifyPersonUpdate(UpdateEvent.UpdateAction type, String id)
   {
      PeopleChangeEventImpl evt = new PeopleChangeEventImpl(type, id);
      listeners.forEach(ears -> {
         notifications.submit(() -> {
            try{
               ears.accept(evt);
            }
            catch(Exception ex)
            {
               logger.log(Level.WARNING, "Call to update people listener failed.", ex);
            }
         });
      });
   }

   @Override
   public AutoCloseable addUpdateListener(Consumer<PersonChangeEvent> ears)
   {
      listeners.add(ears);
      return () -> listeners.remove(ears);
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

   private final class PeopleChangeNotifier extends DataUpdateObserverAdapter<String>
   {
      private final UpdateEvent.UpdateAction type;

      public PeopleChangeNotifier(UpdateEvent.UpdateAction type)
      {
         this.type = type;
      }

      @Override
      public void onFinish(String id)
      {
         notifyPersonUpdate(type, id);
      }
   }
}