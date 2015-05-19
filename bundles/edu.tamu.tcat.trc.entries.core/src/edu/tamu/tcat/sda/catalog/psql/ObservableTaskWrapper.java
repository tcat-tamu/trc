package edu.tamu.tcat.sda.catalog.psql;

import java.sql.Connection;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.datastore.DataUpdateObserver;

public class ObservableTaskWrapper<ResultType> implements SqlExecutor.ExecutorTask<ResultType>
{
   // TODO should be moved into exec framework
   private final DataUpdateObserver<ResultType> observer;
   private final SqlExecutor.ExecutorTask<ResultType> task;

   public ObservableTaskWrapper(SqlExecutor.ExecutorTask<ResultType> task, DataUpdateObserver<ResultType> observer)
   {
      this.task = task;
      this.observer = observer;
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
      try
      {
         checkConnection(conn);

         if (observer.isCanceled())
         {
            observer.aborted();
            return null;
         }

         observer.start();
         return runTask(conn);
      }
      catch(Exception ex)
      {
         if (!observer.isCompleted())
            observer.error(ex.getMessage(), ex);

         throw ex;
      }
   }

   private ResultType runTask(Connection conn) throws Exception
   {
      try
      {
         ResultType result = task.execute(conn);
         observer.finish(result);
         return result;
      }
      catch (Exception e)
      {
         observer.error(e.getMessage(), e);
         throw e;    // TODO should provide wrapper for this exception
      }
   }

   private void checkConnection(Connection conn)
   {
      if (conn == null)
      {
         String msg = "Cannot execute database task. No connection supplied.";
         IllegalStateException ex = new IllegalStateException(msg);
         observer.error(msg, ex);
         throw ex;
      }
   }
}
