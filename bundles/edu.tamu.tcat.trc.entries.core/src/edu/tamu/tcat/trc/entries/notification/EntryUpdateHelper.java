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
 * Provides thread-safe support for registering {@link UpdateListener}s and firing
 * notifications of changes. This is intended to be used by repository implementations to
 * support the task of notifying listeners about updates to data elements.
 */
public final class EntryUpdateHelper<EventType extends UpdateEvent> implements AutoCloseable
{
   private final static Logger logger = Logger.getLogger(EntryUpdateHelper.class.getName());

   private ExecutorService notifications;
   private final CopyOnWriteArrayList<UpdateListener<EventType>> listeners = new CopyOnWriteArrayList<>();

   public EntryUpdateHelper()
   {
      //HACK: make this configurable
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

   public AutoCloseable register(UpdateListener<EventType> ears)
   {
      Objects.requireNonNull(notifications, "This helper has been closed.");

      listeners.add(ears);
      return () -> listeners.remove(ears);
   }

   /**
    * Notifies all registered listeners of that an update to the persistence layer has been
    * executed. This will be executed asynchronously and may return before all listeners have
    * been notifified.
    *
    * @param evt The update action that has been completed.
    */
   public void after(EventType evt)
   {
      Objects.requireNonNull(notifications, "This helper has been closed.");
      listeners.stream().forEach(ears -> fireAfterUpdate(evt, ears));
   }

   /**
    * @param evt The event to fire.
    * @param ears The listener to notify.
    */
   private void fireAfterUpdate(EventType evt, UpdateListener<EventType> ears)
   {
      notifications.submit(() -> {
         try
         {
            ears.handle(evt);
         }
         catch (Exception ex)
         {
            logger.log(Level.WARNING, "Call to update event listener failed for update [" + evt + "].", ex);
         }
      });
   }
}
