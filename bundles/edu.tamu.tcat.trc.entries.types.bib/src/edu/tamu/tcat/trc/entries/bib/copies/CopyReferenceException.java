package edu.tamu.tcat.trc.entries.bib.copies;

public class CopyReferenceException extends Exception
{
   // FIXME poor name. Need to revisit error handling
   public CopyReferenceException()
   {
   }

   public CopyReferenceException(String message)
   {
      super(message);
   }

   public CopyReferenceException(Throwable cause)
   {
      super(cause);
   }

   public CopyReferenceException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public CopyReferenceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
