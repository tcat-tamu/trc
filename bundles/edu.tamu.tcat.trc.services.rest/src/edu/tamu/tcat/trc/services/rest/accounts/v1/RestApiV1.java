package edu.tamu.tcat.trc.services.rest.accounts.v1;

import java.util.List;
import java.util.UUID;

public class RestApiV1
{
   /** a serialization data transfer object for an Account */
   public static class AccountDTO
   {
      public UUID uuid;
      public List<String> roles;
   }

   /** Used for JSON POST login requests */
   public static class LoginRequestDTO
   {
      public String username;
      public String password;
      public String providerId;
   }
}
