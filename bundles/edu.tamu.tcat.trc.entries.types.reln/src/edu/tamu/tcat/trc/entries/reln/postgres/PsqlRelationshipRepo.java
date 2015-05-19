package edu.tamu.tcat.trc.entries.reln.postgres;

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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.catalogentries.IdFactory;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.catalog.psql.ObservableTaskWrapper;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;
import edu.tamu.tcat.trc.entries.reln.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.reln.Relationship;
import edu.tamu.tcat.trc.entries.reln.RelationshipChangeEvent;
import edu.tamu.tcat.trc.entries.reln.RelationshipChangeEvent.ChangeType;
import edu.tamu.tcat.trc.entries.reln.RelationshipNotAvailableException;
import edu.tamu.tcat.trc.entries.reln.RelationshipPersistenceException;
import edu.tamu.tcat.trc.entries.reln.RelationshipRepository;
import edu.tamu.tcat.trc.entries.reln.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.entries.reln.model.RelationshipDV;

public class PsqlRelationshipRepo implements RelationshipRepository
{

   private static final Logger logger = Logger.getLogger(PsqlRelationshipRepo.class.getName());

   public PsqlRelationshipRepo()
   {
   }

   private static final String ID_CONTEXT = "relationships";
   private SqlExecutor exec;
   private IdFactory idFactory;
   private ObjectMapper mapper;
   private RelationshipTypeRegistry typeReg;

   private ExecutorService notifications;

   private final CopyOnWriteArrayList<Consumer<RelationshipChangeEvent>> listeners = new CopyOnWriteArrayList<>();


   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void setIdFactory(IdFactory factory)
   {
      this.idFactory = factory;
   }

   public void setTypeRegistry(RelationshipTypeRegistry typeReg)
   {
      this.typeReg = typeReg;
   }

   public void activate()
   {
      Objects.requireNonNull(exec);
      Objects.requireNonNull(idFactory);

      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      // TODO evalutate choice of executor
      notifications = Executors.newCachedThreadPool();
   }

   public void dispose()
   {
      this.exec = null;
      this.mapper = null;
      this.idFactory = null;

      shutdownNotificationsExec();

      listeners.clear();
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
   public Relationship get(String id) throws RelationshipNotAvailableException, RelationshipPersistenceException
   {
      PsqlGetRelationshipTask task = new PsqlGetRelationshipTask(id, mapper, typeReg);
      try
      {
         return exec.submit(task).get();
      }
      catch (ExecutionException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof RelationshipNotAvailableException)
            throw (RelationshipNotAvailableException)cause;
         if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;

         throw new IllegalStateException("Unexpected problems while attempting to retrieve relationship entry [" + id +"]" , e);
      }
      catch (InterruptedException e) {
         throw new IllegalStateException("Failed to retrieve relationship entry [" + id +"]", e);
      }
   }

   @Override
   public EditRelationshipCommand create() throws RelationshipPersistenceException
   {
      RelationshipDV relationship = new RelationshipDV();
      relationship.id = idFactory.getNextId(ID_CONTEXT);

      EditRelationshipCommandImpl command = new EditRelationshipCommandImpl(relationship, idFactory);
      command.setCommitHook((r) -> {
         PsqlCreateRelationshipTask task = new PsqlCreateRelationshipTask(r, mapper);

         WorkChangeNotifier<String> workChangeNotifier = new WorkChangeNotifier<>(r.id, ChangeType.CREATED);
         ObservableTaskWrapper<String> wrappedTask = new ObservableTaskWrapper<String>(task, workChangeNotifier);

         Future<String> future = exec.submit(wrappedTask);
         return future;
      });
      return command;
   }

   @Override
   public EditRelationshipCommand edit(final String id) throws RelationshipNotAvailableException, RelationshipPersistenceException
   {
      EditRelationshipCommandImpl command = new EditRelationshipCommandImpl(RelationshipDV.create(get(id)) , idFactory);
      command.setCommitHook((r) -> {
         PsqlUpdateRelationshipTask task = new PsqlUpdateRelationshipTask(r, mapper);

         WorkChangeNotifier<String> workChangeNotifier = new WorkChangeNotifier<>(id, ChangeType.MODIFIED);
         ObservableTaskWrapper<String> wrappedTask = new ObservableTaskWrapper<String>(task, workChangeNotifier);

         Future<String> future = exec.submit(wrappedTask);
         return future;
      });
      return command;
   }

   @Override
   public void delete(String id) throws RelationshipNotAvailableException, RelationshipPersistenceException
   {
      PsqlDeleteRelationshipTask deleteTask = new PsqlDeleteRelationshipTask(id);
      WorkChangeNotifier<Void> workChangeNotifier = new WorkChangeNotifier<>(id, ChangeType.DELETED);
      ObservableTaskWrapper<Void> wrappedTask = new ObservableTaskWrapper<>(deleteTask, workChangeNotifier);

      exec.submit(wrappedTask);
   }

   private void notifyRelationshipUpdate(ChangeType type, String relnId)
   {
      RelationshipChangeEventImpl evt = new RelationshipChangeEventImpl(type, relnId);
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
   public AutoCloseable addUpdateListener(Consumer<RelationshipChangeEvent> ears)
   {
      listeners.add(ears);
      return () -> listeners.remove(ears);
   }

   private final class WorkChangeNotifier<ResultType> implements DataUpdateObserver<ResultType>
   {
      private final String id;
      private final ChangeType type;

      public WorkChangeNotifier(String id, ChangeType type)
      {
         this.id = id;
         this.type = type;

      }

      @Override
      public boolean start()
      {
         return true;
      }

      @Override
      public void finish(ResultType result)
      {
         notifyRelationshipUpdate(type, id);
      }

      @Override
      public void aborted()
      {
         // no-op
      }

      @Override
      public void error(String message, Exception ex)
      {
         // no-op
      }

      @Override
      public boolean isCanceled()
      {
         return false;
      }

      @Override
      public boolean isCompleted()
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public State getState()
      {
         throw new UnsupportedOperationException();
      }
   }

   private class RelationshipChangeEventImpl implements RelationshipChangeEvent
   {
      private final ChangeType type;
      private final String id;

      public RelationshipChangeEventImpl(ChangeType type, String id)
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
      public String getRelationshipId()
      {
         return id;
      }

      @Override
      public Relationship getRelationship() throws RelationshipNotAvailableException
      {
         try
         {
            return get(id);
         }
         catch (RelationshipPersistenceException e)
         {
            throw new RelationshipNotAvailableException("Internal error failed to retrieve relationship [" + id + "].", e);
         }
      }

      @Override
      public String toString()
      {
         return "Relationship Change Event: action = " + type + "; id = " + id;
      }
   }

}
