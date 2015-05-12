package edu.tamu.tcat.trc.entries.bio.postgres;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

import edu.tamu.tcat.catalogentries.CatalogRepoException;
import edu.tamu.tcat.catalogentries.IdFactory;
import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.catalog.psql.ExecutionFailedException;
import edu.tamu.tcat.sda.catalog.psql.ObservableTaskWrapper;
import edu.tamu.tcat.sda.datastore.DataUpdateObserverAdapter;
import edu.tamu.tcat.trc.entries.bio.EditPeopleCommand;
import edu.tamu.tcat.trc.entries.bio.PeopleChangeEvent;
import edu.tamu.tcat.trc.entries.bio.PeopleChangeEvent.ChangeType;
import edu.tamu.tcat.trc.entries.bio.PeopleRepository;
import edu.tamu.tcat.trc.entries.bio.Person;
import edu.tamu.tcat.trc.entries.bio.PersonName;
import edu.tamu.tcat.trc.entries.bio.PersonNotAvailableException;
import edu.tamu.tcat.trc.entries.bio.dv.PersonDV;

public class PsqlPeopleRepo implements PeopleRepository
{
   private static final Logger logger = Logger.getLogger(PsqlPeopleRepo.class.getName());

   private final static String GET_PERSON_SQL = "SELECT historical_figure FROM people WHERE id = ?";
   private final static String GET_ALL_SQL = "SELECT historical_figure FROM people WHERE active = true";
   private static final String INSERT_SQL = "INSERT INTO people (historical_figure, id) VALUES(?, ?)";
   private static final String UPDATE_SQL = "UPDATE people SET historical_figure = ?, modified = now() WHERE id = ?";
   private final static String DELETE_SQL =  "UPDATE people SET active = false, modified = now() WHERE id = ?";

   private final PeopleChangeNotifier createdNotifier = new PeopleChangeNotifier(ChangeType.CREATED);
   private final PeopleChangeNotifier modifiedNotifier = new PeopleChangeNotifier(ChangeType.MODIFIED);
   private final PeopleChangeNotifier deletedNotifier = new PeopleChangeNotifier(ChangeType.DELETED);

   private static final String ID_CONTEXT = "people";
   private SqlExecutor exec;
   private IdFactory idFactory;
   private ObjectMapper mapper;

   private ExecutorService notifications;
   private final CopyOnWriteArrayList<Consumer<PeopleChangeEvent>> listeners = new CopyOnWriteArrayList<>();

   public PsqlPeopleRepo()
   {
   }

   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void setIdFactory(IdFactory factory)
   {
      this.idFactory = factory;
   }

   public void activate()
   {
      Objects.requireNonNull(exec);
      Objects.requireNonNull(idFactory);

      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      notifications = Executors.newCachedThreadPool();
   }

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
   public EditPeopleCommand create()
   {
      PersonDV dto = new PersonDV();
      dto.id = idFactory.getNextId(ID_CONTEXT);

      EditPeopleCommandImpl command = new EditPeopleCommandImpl(dto);
      command.setCommitHook((p) -> {
         ObservableTaskWrapper<String> wrappedTask = new ObservableTaskWrapper<String>(makeCreateTask(p), createdNotifier);

         return exec.submit(wrappedTask);
      });


      return command;
   }

   @Override
   public EditPeopleCommand update(PersonDV dto) throws NoSuchCatalogRecordException
   {
      EditPeopleCommandImpl command = new EditPeopleCommandImpl(dto);
      command.setCommitHook((p) -> {
         ObservableTaskWrapper<String> wrappedTask = new ObservableTaskWrapper<String>(makeUpdateTask(p), modifiedNotifier);

         return exec.submit(wrappedTask);
      });


      return command;
   }

   @Override
   public EditPeopleCommand delete(final String personId) throws NoSuchCatalogRecordException
   {
      PersonDV dto = PersonDV.create(get(personId));
      EditPeopleCommandImpl command = new EditPeopleCommandImpl(dto);
      command.setCommitHook((p) -> {
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

   private SqlExecutor.ExecutorTask<String> makeCreateTask(final PersonDV histFigure)
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

   private SqlExecutor.ExecutorTask<String> makeUpdateTask(final PersonDV histFigure)
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


   private PGobject toPGobject(PersonDV dto) throws SQLException, IOException
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
         PersonDV dv = mapper.readValue(json, PersonDV.class);
         return PersonDV.instantiate(dv);
      }
      catch (IOException je)
      {
         // NOTE: possible data leak. If this exception is propagated to someone who isn't authorized to see this record...
         throw new IllegalStateException("Cannot parse person from JSON:\n" + json, je);
      }
   }

   private void notifyPersonUpdate(ChangeType type, String id)
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
   public AutoCloseable addUpdateListener(Consumer<PeopleChangeEvent> ears)
   {
      listeners.add(ears);
      return () -> listeners.remove(ears);
   }

   private class PeopleChangeEventImpl implements PeopleChangeEvent
   {
      private final ChangeType type;
      private final String id;

      public PeopleChangeEventImpl(ChangeType type, String id)
      {
         this.type = type;
         this.id = id;
      }

      @Override
      public ChangeType getChangeType()
      {
         return type;
      }

      @Override
      public String getPersonId()
      {
         return id;
      }

      @Override
      public Person getPerson() throws PersonNotAvailableException
      {
         try
         {
            return get(id);
         }
         catch (NoSuchCatalogRecordException e)
         {
            throw new PersonNotAvailableException("Internal error attempting to retrieve person [" + id + "]");
         }
      }

   }

   private final class PeopleChangeNotifier extends DataUpdateObserverAdapter<String>
   {
      private final ChangeType type;

      public PeopleChangeNotifier(ChangeType type)
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