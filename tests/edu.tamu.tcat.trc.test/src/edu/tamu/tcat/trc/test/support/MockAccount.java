package edu.tamu.tcat.trc.test.support;

import java.util.UUID;

import edu.tamu.tcat.account.Account;

public class MockAccount implements Account
{
   public UUID uid = UUID.randomUUID();
   public String title = "Mock Account";
   public boolean active = true;

   @Override
   public UUID getId()
   {
      return uid;
   }

   @Override
   public boolean isActive()
   {
      return active;
   }

   @Override
   public String getDisplayName()
   {
      return title;
   }

}
