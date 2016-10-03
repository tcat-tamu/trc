package edu.tamu.tcat.trc.entries.core.repo;

public class EntryRepoException extends RuntimeException
{
   public EntryRepoException()
   {
      super();
   }

   public EntryRepoException(String message)
   {
      super(message);
   }

   public EntryRepoException(Throwable cause)
   {
      super(cause);
   }

   public EntryRepoException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public EntryRepoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
