package edu.tamu.tcat.trc.test.persist.postgres;

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
   public String getTitle()
   {
      return title;
   }

   @Override
   public boolean isActive()
   {
      return active;
   }

}
