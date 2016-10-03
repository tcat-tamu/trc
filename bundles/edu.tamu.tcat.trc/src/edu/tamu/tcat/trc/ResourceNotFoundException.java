package edu.tamu.tcat.trc;

/**
 * Indicates that a given resource could not be found.
 */
public class ResourceNotFoundException extends TrcException
{
   public ResourceNotFoundException()
   {
      super();
   }

   public ResourceNotFoundException(String message)
   {
      super(message);
   }

   public ResourceNotFoundException(Throwable cause)
   {
      super(cause);
   }

   public ResourceNotFoundException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public ResourceNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
