package edu.tamu.tcat.trc.entries.repo;

import java.sql.SQLException;

/**
 * Represents an internal error the the execution of a request against the underlying
 * repository that cannot be corrected by application behavior.  {@link SQLException} errors
 * are one example of this as the corresponding checked exception typically results from a
 * programming or system configuration error (malformed SQL statement or mismatch with the DB).
 */
public class ExecutionFailedException extends RuntimeException
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
