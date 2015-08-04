package edu.tamu.tcat.trc.refman;

public class RefManagerException extends Exception
{

   public RefManagerException()
   {
   }

   public RefManagerException(String message)
   {
      super(message);
   }

   public RefManagerException(Throwable cause)
   {
      super(cause);
   }

   public RefManagerException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public RefManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
