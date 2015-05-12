package edu.tamu.tcat.trc.resources.books.resolve;

public class UnsupportedCopyTypeException extends Exception
{

   public UnsupportedCopyTypeException()
   {
   }

   public UnsupportedCopyTypeException(String message)
   {
      super(message);
   }

   public UnsupportedCopyTypeException(Throwable cause)
   {
      super(cause);
   }

   public UnsupportedCopyTypeException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public UnsupportedCopyTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
