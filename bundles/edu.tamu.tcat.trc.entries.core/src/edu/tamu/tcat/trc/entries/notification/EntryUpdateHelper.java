package edu.tamu.tcat.trc.entries.notification;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides thread-safe support for registering {@link UpdateListener}s and fire before/after
 * notifications of changes. This is intended to be used by repository implementations to
 * support the task of notifying listeners about updates to
 *
 * @param <T>
 */
public final class EntryUpdateHelper<T> implements AutoCloseable
{
   private final static Logger logger = Logger.getLogger(EntryUpdateHelper.class.getName());

   private ExecutorService notifications;
   private final CopyOnWriteArrayList<UpdateListener<T>> listeners = new CopyOnWriteArrayList<>();

   public EntryUpdateHelper()
   {
      notifications = Executors.newCachedThreadPool();
   }

   @Override
   public void close()
   {
      shutdownNotificationsExec();
      notifications = null;
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

   public AutoCloseable register(UpdateListener<T> ears)
   {
      Objects.requireNonNull(notifications, "This helper has been closed.");

      listeners.add(ears);
      return () -> listeners.remove(ears);
   }

   /**
    * Notifies all registered listeners of the pending update and waits until all notifications
    * have been processed. If any listener returns <code>false</code>, this method will return
    * <code>false</code> indicating that the proposed update should be canceled.
    *
    * @param evt The update action that is about to execute.
    * @return <code>true</code> if all supplied listeners return <code>true</code>,
    *    <code>false</code> if any registered listener votes to cancel the update.
    */
   public boolean before(UpdateEvent<T> evt)
   {
      // TODO should we throw an 'UpdatePreventedException' rather than simply return false?
      Objects.requireNonNull(notifications, "This helper has been closed.");
      return listeners.stream().parallel().allMatch(ears -> fireBeforeUpdate(evt, ears));
   }

   /**
    * Notifies all registered listeners of that an update to the persistence layer has been
    * executed. This will be executed asynchronously and may return before all listeners have
    * been notifified.
    *
    * @param evt The update action that has been completed.
    */
   public void after(UpdateEvent<T> evt)
   {
      Objects.requireNonNull(notifications, "This helper has been closed.");
      listeners.stream().forEach(ears -> fireAfterUpdate(evt, ears));
   }

   /**
    * @param evt The event to fire.
    * @param ears The listener to notify.
    * @return {@code false} of the update should be canceled.
    */
   private boolean fireBeforeUpdate(UpdateEvent<T> evt, UpdateListener<T> ears)
   {
      try
      {
         return ears.beforeUpdate(evt);
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Call to update event listener failed for update [" + evt + "].", ex);
         return false;
      }
   }

   /**
    * @param evt The event to fire.
    * @param ears The listener to notify.
    */
   private void fireAfterUpdate(UpdateEvent<T> evt, UpdateListener<T> ears)
   {
      notifications.submit(() -> {
         try
         {
            ears.afterUpdate(evt);
         }
         catch (Exception ex)
         {
            logger.log(Level.WARNING, "Call to update event listener failed for update [" + evt + "].", ex);
         }
      });
   }
}
