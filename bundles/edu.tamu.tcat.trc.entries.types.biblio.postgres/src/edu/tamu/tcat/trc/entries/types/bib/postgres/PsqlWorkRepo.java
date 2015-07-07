package edu.tamu.tcat.trc.entries.types.bib.postgres;

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
import edu.tamu.tcat.db.exec.sql.SqlExecutor.ExecutorTask;
import edu.tamu.tcat.trc.entries.core.IdFactory;
import edu.tamu.tcat.trc.entries.notification.BaseUpdateEvent;
import edu.tamu.tcat.trc.entries.notification.DataUpdateObserverAdapter;
import edu.tamu.tcat.trc.entries.notification.ObservableTaskWrapper;
import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDV;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkChangeEvent;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;

public class PsqlWorkRepo implements WorkRepository
{
   private static final Logger logger = Logger.getLogger(PsqlWorkRepo.class.getName());

   private final static String GET_SQL = "SELECT work FROM works WHERE id=?";
   private final static String LIST_WORKS_SQL = "SELECT work FROM works WHERE active = true";
   private final static String UPDATE_WORK_SQL = "UPDATE works SET work = ?, modified = now() WHERE id = ?";
   private final static String CREATE_WORK_SQL = "INSERT INTO works (work, id) VALUES(?, ?)";
   private final static String DELETE_SQL = "UPDATE works SET active = false WHERE id = ?";

   //HACK: doesn't really matter for now, but once authz is in place, this will be the user's id
   private static final UUID ACCOUNT_ID_REPO = UUID.randomUUID();

   public static final String WORK_CONTEXT = "works";

   private SqlExecutor exec;
   private ObjectMapper mapper;
   private PeopleRepository peopleRepo;

   // FIXME replace with listener service
   private ExecutorService notifications;

   private final CopyOnWriteArrayList<Consumer<WorkChangeEvent>> listeners = new CopyOnWriteArrayList<>();

   private IdFactory idFactory;

   public PsqlWorkRepo()
   {
   }

   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void setPeopleRepo(PeopleRepository repo)
   {
      this.peopleRepo = repo;
   }

   public void setIdFactory(IdFactory idFactory)
   {
      this.idFactory = idFactory;
   }

   public void activate()
   {
      Objects.requireNonNull(exec);

      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      notifications = Executors.newCachedThreadPool();
   }

   public void dispose()
   {
      this.exec = null;
      this.mapper = null;
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
   public Person getAuthor(AuthorReference ref)
   {
      String id = ref.getId();
      try {
         return peopleRepo.get(id);
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Could not retrieve person instance (" + id + ").", ex);
      }
   }

   @Override
   public Iterable<Work> listWorks()
   {
      Future<Iterable<Work>> submit = exec.submit(makeListWorksTask());
      try
      {
         return submit.get();
      }
      catch (ExecutionException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;

         throw new IllegalStateException("Unexpected problems while attempting to retrieve work records " , e);
      }
      catch (InterruptedException e)
      {
         throw new IllegalStateException("Failed to retrieve work records", e);
      }
   }


   @Override
   public Iterable<Work> listWorks(String titleName)
   {
      List<Work> workResults = new ArrayList<>();
      Iterable<Work> listWorks = listWorks();
      titleName = titleName.toLowerCase();

      for (Work w : listWorks)
      {
         if (hasTitle(w, titleName))
            workResults.add(w);
      }

      return workResults;
   }

   private boolean hasTitle(Work w, String name)
   {
      for (Title t : w.getTitle().getAlternateTitles())
      {
         if (hasTitleName(t, name)) {
            return true;
         }
      }

      return false;
   }

   private boolean hasTitleName(Title title, String titleName)
   {
      String test = title.getFullTitle();
      if (test != null && test.toLowerCase().contains(titleName))
         return true;

      test = title.getTitle();
      if (test != null && test.toLowerCase().contains(titleName))
         return true;

      return false;
   }

   @Override
   public Work getWork(String workId) throws NoSuchCatalogRecordException
   {
      try
      {
         return exec.submit(makeGetWorkTask(workId)).get();
      }
      catch (ExecutionException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof NoSuchCatalogRecordException)
            throw (NoSuchCatalogRecordException)cause;
         if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;

         throw new IllegalStateException("Unexpected problems while attempting to retrieve bibliographic entry [" + workId +"]" , e);
      }
      catch (InterruptedException e) {
         throw new IllegalStateException("Failed to retrieve bibliographic entry [" + workId +"]", e);
      }
   }

   @Override
   public Edition getEdition(String workId, String editionId) throws NoSuchCatalogRecordException
   {
      Work work = getWork(workId);
      return work.getEdition(editionId);
   }

   @Override
   public Volume getVolume(String workId, String editionId, String volumeId) throws NoSuchCatalogRecordException
   {
      // TODO pull from DB directly
      Work work = getWork(workId);
      Edition edition = work.getEdition(editionId);
      return edition.getVolume(volumeId);
   }

   @Override
   public EditWorkCommand create()
   {
      String id = idFactory.getNextId(WORK_CONTEXT);
      return create(id);
   }

   @Override
   public EditWorkCommand create(String id)
   {
      WorkDV work = new WorkDV();
      work.id = id;
      EditWorkCommandImpl command = new EditWorkCommandImpl(work, idFactory);
      command.setCommitHook(dto -> updateWork(dto, UpdateEvent.UpdateAction.CREATE));
      return command;
   }

   @Override
   public EditWorkCommand edit(String id) throws NoSuchCatalogRecordException
   {
      Work work = getWork(id);
      EditWorkCommandImpl command = new EditWorkCommandImpl(WorkDV.create(work), idFactory);
      command.setCommitHook(dto -> updateWork(dto, UpdateEvent.UpdateAction.UPDATE));
      return command;
   }

   public Future<String> updateWork(WorkDV workDv, UpdateEvent.UpdateAction changeType)
   {
      String sql = getUpdateSql(changeType);

      try
      {
         String json = mapper.writeValueAsString(workDv);
         SqlExecutor.ExecutorTask<String> task = makeUpdateTask(workDv.id, json, sql);

         WorkUpdateNotifier updateNotifier = new WorkUpdateNotifier(changeType);
         ObservableTaskWrapper<String> wrapTask = new ObservableTaskWrapper<String>(task, updateNotifier);

         Future<String> workId = exec.submit(wrapTask);
         return workId;
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to update work [" + workDv.id + "]", e);
      }
   }

   @Override
   public void delete(String id)
   {
      ExecutorTask<String> task = makeDeleteTask(id);
      exec.submit(new ObservableTaskWrapper<String>(task, new WorkUpdateNotifier(UpdateEvent.UpdateAction.DELETE)));
   }

   private String getUpdateSql(UpdateEvent.UpdateAction changeType)
   {
      String sql;
      switch (changeType)
      {
         case UPDATE:
            sql = UPDATE_WORK_SQL;
            break;
         case CREATE:
            sql = CREATE_WORK_SQL;
            break;
         default:
            throw new IllegalArgumentException("Change type must be created or modified [" + changeType + "]");
      }
      return sql;
   }

   private SqlExecutor.ExecutorTask<Work> makeGetWorkTask(String workId)
   {
      return (conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(GET_SQL))
         {
            ps.setString(1, workId);
            try (ResultSet rs = ps.executeQuery())
            {
               if (!rs.next())
                  throw new NoSuchCatalogRecordException("No catalog record exists for work id=" + workId);

               PGobject pgo = (PGobject)rs.getObject("work");
               String workJson = pgo.toString();
               try
               {
                  WorkDV dv = mapper.readValue(workJson, WorkDV.class);
                  return WorkDV.instantiate(dv);
               }
               catch (IOException e)
               {
                  throw new IllegalStateException("Failed to parse bibliographic record\n" + workJson, e);
               }
            }
         }
         catch (SQLException e)
         {
            throw new IllegalStateException("Failed to retrieve bibliographic entry [entry id = " + workId + "]", e);
         }
      };
   }

   private SqlExecutor.ExecutorTask<Iterable<Work>> makeListWorksTask()
   {
      return (conn) -> {
         List<Work> works = new ArrayList<>();
         try (PreparedStatement ps = conn.prepareStatement(LIST_WORKS_SQL);
              ResultSet rs = ps.executeQuery())
         {
            while(rs.next())
            {
               PGobject pgo = (PGobject)rs.getObject("work");
               String workJson = pgo.toString();
               try
               {
                  WorkDV dv = mapper.readValue(workJson, WorkDV.class);
                  works.add(WorkDV.instantiate(dv));
               }
               catch (IOException e)
               {
                  throw new IllegalStateException("Failed to parse bibliographic record\n" + workJson, e);
               }
            }

            return works;
         }
         catch (SQLException e)
         {
            throw new IllegalStateException("Failed to list bibliographic entries", e);
         }
      };
   }

   private SqlExecutor.ExecutorTask<String> makeUpdateTask(String id, String json, String sql)
   {
      return (conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(sql))
         {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("json");
            jsonObject.setValue(json);

            ps.setObject(1, jsonObject);
            ps.setString(2, id);

            int ct = ps.executeUpdate();
            if (ct != 1)
               throw new IllegalStateException("Failed to update work. Unexpected number of rows updates [" + ct + "]");

            return id;
         }
         catch(SQLException e)
         {
            throw new IllegalStateException("Failed to update work: [" + id + "].\n" + json);
         }
      };
   }

   private SqlExecutor.ExecutorTask<String> makeDeleteTask(String id)
   {
      return (conn) -> {
         try (PreparedStatement ps = conn.prepareCall(DELETE_SQL))
         {
            ps.setString(1, id);

            int ct = ps.executeUpdate();
            if (ct != 1)
               throw new IllegalStateException("Failed to de-activate work, id: [" + id + "]. Unexpected number of rows updated [" + ct + "]");

            return id;
         }
         catch(SQLException e)
         {
            throw new IllegalStateException("Failed to de-activate work: [" + id + "]");
         }
      };
   }

   private void notifyUpdate(UpdateEvent.UpdateAction type, String relnId)
   {
      WorksChangeEventImpl evt = new WorksChangeEventImpl(type, relnId);
      listeners.forEach(ears -> {
         notifications.submit(() -> {
            try {
               ears.accept(evt);
            } catch (Exception ex) {
               logger.log(Level.WARNING, "Call to update listener failed.", ex);
            }
         });
      });
   }

   @Override
   public AutoCloseable addUpdateListener(Consumer<WorkChangeEvent> ears)
   {
      listeners.add(ears);
      return () -> listeners.remove(ears);
   }

   private class WorksChangeEventImpl extends BaseUpdateEvent implements WorkChangeEvent
   {
      public WorksChangeEventImpl(UpdateEvent.UpdateAction type, String id)
      {
         super(id, type, ACCOUNT_ID_REPO, Instant.now());
      }

      @Override
      public Work getWork() throws CatalogRepoException
      {
         try
         {
            return PsqlWorkRepo.this.getWork(id);
         }
         catch (NoSuchCatalogRecordException e)
         {
            throw new CatalogRepoException("Failed retrieving work [" + id + "]", e);
         }
      }

      @Override
      public String toString()
      {
         return "Work Change Event " + super.toString();
      }
   }

   private final class WorkUpdateNotifier extends DataUpdateObserverAdapter<String>
   {
      private final UpdateEvent.UpdateAction type;

      public WorkUpdateNotifier(UpdateEvent.UpdateAction type)
      {
         this.type = type;
      }

      @Override
      public void onFinish(String id)
      {
         notifyUpdate(type, id);
      }
   }

   /**
    * @param work
    * @return Context for generating IDs for Editions within a Work.
    */
   public static String getContext(WorkDV work)
   {
      return WORK_CONTEXT + "/" + work.id;
   }

   /**
    * @param work
    * @param edition
    * @return Context for generating IDs for Volumes within an Edition (subs. w/in a Work).
    */
   public static String getContext(WorkDV work, EditionDV edition)
   {
      return getContext(work) + "/" + edition.id;
   }
}
