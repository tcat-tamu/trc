package edu.tamu.tcat.catalogentries;

/**
 *  Thrown when data updates or retrieval attempt to supply invalid or inconsistent data.
 *  The supplied message should be suitable to reporting to client applications and
 *  potentially users to aid error reporting and ongoing product support.
 *
 */
public class InvalidDataException extends RuntimeException
{

   // TODO should probably be checked.

   public InvalidDataException()
   {
   }

   public InvalidDataException(String message)
   {
      super(message);
   }

   public InvalidDataException(Throwable cause)
   {
      super(cause);
   }

   public InvalidDataException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public InvalidDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
