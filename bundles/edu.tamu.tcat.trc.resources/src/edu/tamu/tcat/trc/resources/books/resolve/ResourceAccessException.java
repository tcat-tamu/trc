package edu.tamu.tcat.trc.resources.books.resolve;

public class ResourceAccessException extends Exception
{

   public ResourceAccessException()
   {
   }

   public ResourceAccessException(String message)
   {
      super(message);
   }

   public ResourceAccessException(Throwable cause)
   {
      super(cause);
   }

   public ResourceAccessException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public ResourceAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
