package edu.tamu.tcat.trc.search;

/**
 * A base exception type for entry types to use within their search modules.
 */
public class SearchException extends Exception
{
   public SearchException()
   {
   }

   public SearchException(String message)
   {
      super(message);
   }

   public SearchException(Throwable cause)
   {
      super(cause);
   }

   public SearchException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public SearchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
