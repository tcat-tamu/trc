package edu.tamu.tcat.sda.datastore;

/**
 * Thrown to indicate that a data store is unavailable or incorrectly configured.
 */
public class DataStoreException extends Exception
{

   public DataStoreException()
   {
   }

   public DataStoreException(String message)
   {
      super(message);
   }

   public DataStoreException(Throwable cause)
   {
      super(cause);
   }

   public DataStoreException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public DataStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
