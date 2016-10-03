package edu.tamu.tcat.trc;

/**
 * Parent exception for TRC related exceptions.
 *
 *
 */
public class TrcException extends RuntimeException
{

   public TrcException()
   {
      super();
   }

   public TrcException(String message)
   {
      super(message);
   }

   public TrcException(Throwable cause)
   {
      super(cause);
   }

   public TrcException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public TrcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
