package edu.tamu.tcat.trc.entries.bib;

public class WorkException extends Exception
{

   public WorkException()
   {
   }

   public WorkException(String message)
   {
      super(message);
   }

   public WorkException(Throwable cause)
   {
      super(cause);
   }

   public WorkException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public WorkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
