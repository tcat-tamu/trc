package edu.tamu.tcat.sda.catalog.psql;

public class ExecutionFailedException extends Exception
{

   public ExecutionFailedException()
   {
   }

   public ExecutionFailedException(String message)
   {
      super(message);
   }

   public ExecutionFailedException(Throwable cause)
   {
      super(cause);
   }

   public ExecutionFailedException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public ExecutionFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
