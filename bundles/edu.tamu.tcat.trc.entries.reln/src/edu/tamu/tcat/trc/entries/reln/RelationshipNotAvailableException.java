package edu.tamu.tcat.trc.entries.reln;

/**
 * Thrown by methods that attempt to access a relationship that does not exist.
 * This typically happens in an attempt to retrieve a relationship by ID when
 * no relationship with that ID exists or the relationship has been deleted.
 */
public class RelationshipNotAvailableException extends Exception
{

   public RelationshipNotAvailableException()
   {
   }

   public RelationshipNotAvailableException(String message)
   {
      super(message);
   }

   public RelationshipNotAvailableException(Throwable cause)
   {
      super(cause);
   }

   public RelationshipNotAvailableException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public RelationshipNotAvailableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
