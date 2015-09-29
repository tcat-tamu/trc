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
package edu.tamu.tcat.trc.entries.types.reln.postgres;

import java.time.Instant;
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.notification.BaseUpdateEvent;
import edu.tamu.tcat.trc.entries.notification.DataUpdateObserverAdapter;
import edu.tamu.tcat.trc.entries.notification.ObservableTaskWrapper;
import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipChangeEvent;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipPersistenceException;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.persist.IdFactory;

public class PsqlRelationshipRepo implements RelationshipRepository
{
   private static final Logger logger = Logger.getLogger(PsqlRelationshipRepo.class.getName());

   //HACK: doesn't really matter for now, but once authz is in place, this will be the user's id
   private static final UUID ACCOUNT_ID_REPO = UUID.randomUUID();

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
   public Relationship get(String id) throws CatalogRepoException
   {
      PsqlGetRelationshipTask task = new PsqlGetRelationshipTask(id, mapper, typeReg);
      try
      {
         return exec.submit(task).get();
      }
      catch (ExecutionException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof RelationshipPersistenceException)
            throw (RelationshipPersistenceException)cause;
         if (cause instanceof CatalogRepoException)
            throw (CatalogRepoException)cause;
         if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;

         throw new IllegalStateException("Unexpected problems while attempting to retrieve relationship entry [" + id +"]" , e);
      }
      catch (InterruptedException e) {
         throw new IllegalStateException("Failed to retrieve relationship entry [" + id +"]", e);
      }
   }

   @Override
   public EditRelationshipCommand create() throws CatalogRepoException
   {
      RelationshipDTO relationship = new RelationshipDTO();
      relationship.id = idFactory.getNextId(ID_CONTEXT);

      EditRelationshipCommandImpl command = new EditRelationshipCommandImpl(relationship, idFactory);
      command.setCommitHook((r) -> {
         PsqlCreateRelationshipTask task = new PsqlCreateRelationshipTask(r, mapper);

         RelnChangeNotifier<String> workChangeNotifier = new RelnChangeNotifier<>(r.id, UpdateEvent.UpdateAction.CREATE);
         ObservableTaskWrapper<String> wrappedTask = new ObservableTaskWrapper<String>(task, workChangeNotifier);

         Future<String> future = exec.submit(wrappedTask);
         return future;
      });
      return command;
   }

   @Override
   public EditRelationshipCommand edit(final String id) throws CatalogRepoException
   {
      EditRelationshipCommandImpl command = new EditRelationshipCommandImpl(RelationshipDTO.create(get(id)) , idFactory);
      command.setCommitHook((r) -> {
         PsqlUpdateRelationshipTask task = new PsqlUpdateRelationshipTask(r, mapper);

         RelnChangeNotifier<String> workChangeNotifier = new RelnChangeNotifier<>(id, UpdateEvent.UpdateAction.UPDATE);
         ObservableTaskWrapper<String> wrappedTask = new ObservableTaskWrapper<String>(task, workChangeNotifier);

         Future<String> future = exec.submit(wrappedTask);
         return future;
      });
      return command;
   }

   @Override
   public void delete(String id) throws CatalogRepoException
   {
      PsqlDeleteRelationshipTask deleteTask = new PsqlDeleteRelationshipTask(id);
      RelnChangeNotifier<Void> workChangeNotifier = new RelnChangeNotifier<>(id, UpdateEvent.UpdateAction.DELETE);
      ObservableTaskWrapper<Void> wrappedTask = new ObservableTaskWrapper<>(deleteTask, workChangeNotifier);

      exec.submit(wrappedTask);
   }

   private void notifyRelationshipUpdate(UpdateEvent.UpdateAction type, String relnId)
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

   private final class RelnChangeNotifier<ResultType> extends DataUpdateObserverAdapter<ResultType>
   {
      private final String id;
      private final UpdateEvent.UpdateAction type;

      public RelnChangeNotifier(String id, UpdateEvent.UpdateAction type)
      {
         this.id = id;
         this.type = type;

      }

      @Override
      protected void onFinish(ResultType result)
      {
         notifyRelationshipUpdate(type, id);
      }
   }

   private class RelationshipChangeEventImpl extends BaseUpdateEvent implements RelationshipChangeEvent
   {
      public RelationshipChangeEventImpl(UpdateEvent.UpdateAction type, String id)
      {
         super(id, type, ACCOUNT_ID_REPO, Instant.now());
      }

      @Override
      public String toString()
      {
         return "Relationship Change " + super.toString();
      }
   }
}
