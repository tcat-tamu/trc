package org.tamu.tcat.trc.persist.postgres;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;

public class NotifyingTaskFactory implements AutoCloseable
{
   // TODO replace with Guava ListenableFutures?
   private final static Logger logger = Logger.getLogger(NotifyingTaskFactory.class.getName());

   private volatile boolean closed = false;
   private ExecutorService notifications;

   public NotifyingTaskFactory()
   {
      //HACK: make this configurable
      notifications = Executors.newCachedThreadPool();
   }
   public NotifyingTaskFactory(int numThreads)
   {
      //HACK: make this configurable
      notifications = Executors.newFixedThreadPool(numThreads);
   }

   @Override
   public void close()
   {
      closed = true;
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

   public <X> ObservableTask<X> wrap(SqlExecutor.ExecutorTask<X> task)
   {
      if (closed)
         throw new IllegalStateException("Task factory has been closed.");

      // TODO keep track of tasks and pause closing untill all have been completed or timeout reached
      return new TaskWrapper<X>(task);
   }


   public interface TaskObserver<T>
   {
      void onFinished(T result);

      default void onError(Exception ex)
      {
         // no-op by default
      };
   }

   public interface ObservableTask<T> extends SqlExecutor.ExecutorTask<T>
   {
      AutoCloseable afterExecution(TaskObserver<T> ears);
   }

   /**
    * Encapsulates a {code DataUpdateObserver} into this {@link SqlExecutor.ExecutorTask}, managing the
    * observer's lifecycle and task execution, passing the task result into the observer upon completion.
    */
   public class TaskWrapper<ResultType> implements ObservableTask<ResultType>
   {
      // TODO should be moved into exec framework
      private final SqlExecutor.ExecutorTask<ResultType> task;
      private final CopyOnWriteArrayList<TaskObserver<ResultType>> listeners = new CopyOnWriteArrayList<>();

      public volatile boolean executed = false;

      private TaskWrapper(SqlExecutor.ExecutorTask<ResultType> task)
      {
         this.task = task;
      }

      /**
       * Listeners must be attached prior to execution.
       *
       * @param ears
       * @return
       */
      @Override
      public AutoCloseable afterExecution(TaskObserver<ResultType> ears)
      {
         if (executed)
            throw new IllegalStateException("Task has already executed");

         listeners.add(ears);
         return () -> listeners.remove(ears);
      }

      /**
       * In the event that either the wrapped task or the observer throw an exception, this
       * will attempt to notify the observer and propagate the exception. Since tasks will
       * typically be run in an {@code Executor}, this make the exception available to clients
       * via the Java concurrency API's {@code Future} interface.
       *
       * <p>
       * Note that this may return null if the task is canceled by the observer or if the
       * underlying task returns null.
       */
      @Override
      public ResultType execute(Connection conn) throws Exception
      {
         if (notifications.isShutdown())
            throw new IllegalStateException("The containing notifications factory has been shut down");
         if (executed)
            throw new IllegalStateException("Task has already executed");

         executed = true;
         try
         {
            ResultType result = task.execute(conn);
            finished(result);
            return result;
         }
         catch(Exception ex)
         {
            error(ex);
            throw ex;
         }
      }

      private void finished(ResultType result)
      {
         if (notifications.isShutdown())
            throw new IllegalStateException();

         listeners.forEach(ears -> notifications.submit(() -> ears.onFinished(result)));
         listeners.clear();
      }

      private void error(Exception ex)
      {
         if (notifications.isShutdown())
            throw new IllegalStateException();

         listeners.forEach(ears -> notifications.submit(() -> ears.onError(ex)));
         listeners.clear();
      }

   }
}
