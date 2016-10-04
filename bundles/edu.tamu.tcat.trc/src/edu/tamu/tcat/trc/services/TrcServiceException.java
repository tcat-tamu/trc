package edu.tamu.tcat.trc.services;

import edu.tamu.tcat.trc.TrcException;

public class TrcServiceException extends TrcException
{

   public TrcServiceException()
   {
      super();
   }

   public TrcServiceException(String message)
   {
      super(message);
   }

   public TrcServiceException(Throwable cause)
   {
      super(cause);
   }

   public TrcServiceException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public TrcServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
