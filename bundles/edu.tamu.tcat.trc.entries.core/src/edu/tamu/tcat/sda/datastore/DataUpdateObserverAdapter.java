package edu.tamu.tcat.sda.datastore;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a base implementation of the {@link DataUpdateObserver} that implements the
 * main API methods in order to maintain the state contract of the API. The methods delegate
 * to {@code on*} methods (e.g., {@link #onFinish(Object)}) to allow extensions to handle
 * events in the lifecycle of a database task. Default no-op implementations are provided for
 * these methods to allow sub-classes to selectively override the methods they are interested in.
 *
 * @param <R> The result type of the database task.
 */

public class DataUpdateObserverAdapter<R> implements DataUpdateObserver<R>
{
   private static final Logger logger = Logger.getLogger("org.tamu.tcat.sda.datastore.errors");

   private volatile boolean cancelled = false;
   private volatile State state = State.PENDING;

   public DataUpdateObserverAdapter()
   {
   }


   /**
    * Implements the {@link #start()} method to maintain the state contract of the interface
    * and delegates event handling to the {@link #onStart()} method.
    */
   @Override
   public final boolean start()
   {
      synchronized (state)
      {
         if (isCompleted())
            throw new IllegalStateException();

         try
         {
            if (this.onStart())
            {
               state = State.STARTED;
               return true;
            }
         }
         catch (Exception e)
         {
            logger.log(Level.SEVERE, "DataUpdateAdapter error: onStart failed [" + this + "]", e);
         }

         return false;
      }
   }

   /**
    * Implements the {@link #finish()} method to maintain the state contract of the interface
    * and delegates event handling to the {@link #onFinish(Object)} method. Provides a fault
    * barrier to block exceptions in third party code
    *
    * @throws IllegalStateException if the update process has already been completed.
    */
   @Override
   public final void finish(R result)
   {
      try
      {
         synchronized (state)
         {
            if (isCompleted())
               throw new IllegalStateException();

            this.onFinish(result);
            state = State.COMPLETED;
         }
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "DataUpdateAdapter error: onFinish failed [" + this + "]", e);
      }
   }

   /**
    * Implements the {@link #aborted()} method to maintain the state contract of the interface
    * and delegates event handling to the {@link #onAborted()} method. Provides a fault
    * barrier to block exceptions in third party code
    *
    * @throws IllegalStateException if the update process has already been completed.
    */
   @Override
   public void aborted()
   {
      try
      {
         synchronized (state)
         {
            if (isCompleted())
               throw new IllegalStateException();

            this.onAborted();
            state = State.ABORTED;
         }
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "DataUpdateAdapter error: onAborted failed [" + this + "]", e);
      }

   }

   /**
    * Implements the {@link #error(String, Exception)} method to maintain the state contract
    * of the interface and delegates event handling to the {@link #onError(String, Exception)}
    * method. Provides a fault barrier to block exceptions in third party code
    *
    * @throws IllegalStateException if the update process has already been completed.
    */
   @Override
   public final void error(String message, Exception ex)
   {
      synchronized (state)
      {
         if (isCompleted())
            throw new IllegalStateException();

         this.state = State.ERROR;

         try
         {
            this.onError(message, ex);
         }
         catch (Exception e)
         {
            e.addSuppressed(ex);
            logger.log(Level.SEVERE, "DataUpdateAdapter error: onError failed. '" + message + "' [" + ex + "]", e);
         }
      }
   }

   @Override
   public final boolean isCanceled()
   {
      return cancelled;
   }

   public final void cancel()
   {
      this.cancelled = true;
   }

   @Override
   public final boolean isCompleted()
   {
      return !(state == State.PENDING || state == State.STARTED);
   }

   @Override
   public final State getState()
   {
      return state;
   }

   protected void onAborted()
   {
      // default no-op implementation

   }

   /**
    * Allows
    * @return {@code false} If processing should be aborted at this stage.
    */
   protected boolean onStart() {
      return true;
   }

   protected void onFinish(R result)
   {
      // default no-op implementation
   }

   protected void onError(String message, Exception ex)
   {
      // default no-op implementation
   }

}
