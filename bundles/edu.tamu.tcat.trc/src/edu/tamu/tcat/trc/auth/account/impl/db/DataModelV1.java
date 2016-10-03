package edu.tamu.tcat.trc.auth.account.impl.db;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class DataModelV1
{
   public static class AccountData
   {
      public UUID uuid;
      public String displayName;
      public boolean active;
      public String username;
      public String title;
      public String firstName;
      public String lastName;
      public String email;
      public String affiliation;

      public Map<String, String> properties;

      public static AccountData copy(AccountData orig)
      {
         AccountData dto = new AccountData();
         dto.uuid = orig.uuid;
         dto.displayName = orig.displayName;
         dto.active = orig.active;
         dto.username = orig.username;
         dto.title = orig.title;
         dto.firstName = orig.firstName;
         dto.lastName = orig.lastName;
         dto.email = orig.email;
         dto.affiliation = orig.affiliation;

         dto.properties = new HashMap<>(orig.properties);

         return dto;
      }
   }
}
