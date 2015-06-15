package edu.tamu.tcat.trc.entries.types.reln.repo;

import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;

/**
 * Indicates that an attempt to access the underlying persistence layer failed for some reason.
 */
public class RelationshipPersistenceException extends CatalogRepoException
{
   public RelationshipPersistenceException()
   {
   }

   public RelationshipPersistenceException(String message)
   {
      super(message);
   }

   public RelationshipPersistenceException(Throwable cause)
   {
      super(cause);
   }

   public RelationshipPersistenceException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public RelationshipPersistenceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
