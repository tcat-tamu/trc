package edu.tamu.tcat.trc.entries.bio.rest.v1;

/**
 * Indicates that a resource could not be created.  
 */
public class ResourceCreationException extends Exception
{

   public ResourceCreationException()
   {
   }

   public ResourceCreationException(String message)
   {
      super(message);
   }

   public ResourceCreationException(Throwable cause)
   {
      super(cause);
   }

   public ResourceCreationException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public ResourceCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
