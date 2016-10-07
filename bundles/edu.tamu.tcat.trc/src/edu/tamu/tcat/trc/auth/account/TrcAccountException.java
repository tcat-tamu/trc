package edu.tamu.tcat.trc.auth.account;

import edu.tamu.tcat.trc.TrcException;

public class TrcAccountException extends TrcException
{

   public TrcAccountException()
   {
   }

   public TrcAccountException(String message)
   {
      super(message);
   }

   public TrcAccountException(Throwable cause)
   {
      super(cause);
   }

   public TrcAccountException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public TrcAccountException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
