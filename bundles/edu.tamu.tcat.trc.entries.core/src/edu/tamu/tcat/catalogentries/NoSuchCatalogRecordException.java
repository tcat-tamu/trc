package edu.tamu.tcat.catalogentries;

/**
 * Indicates that a catalog record (such as a bibliographic entry or biographical
 * details) does not exist. Typically thrown when attempting to access an entry by
 * a unique identifier.
 */
public class NoSuchCatalogRecordException extends Exception
{
   public NoSuchCatalogRecordException()
   {
   }

   public NoSuchCatalogRecordException(String message)
   {
      super(message);
   }

   public NoSuchCatalogRecordException(Throwable cause)
   {
      super(cause);
   }

   public NoSuchCatalogRecordException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public NoSuchCatalogRecordException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
