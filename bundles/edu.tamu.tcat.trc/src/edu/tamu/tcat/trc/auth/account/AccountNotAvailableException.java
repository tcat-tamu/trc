package edu.tamu.tcat.trc.auth.account;

public class AccountNotAvailableException extends TrcAccountException
{

   public AccountNotAvailableException()
   {
   }

   public AccountNotAvailableException(String message)
   {
      super(message);
   }

   public AccountNotAvailableException(Throwable cause)
   {
      super(cause);
   }

   public AccountNotAvailableException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public AccountNotAvailableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
