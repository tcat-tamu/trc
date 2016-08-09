package edu.tamu.tcat.trc.categorization.tests;

import java.util.UUID;

import edu.tamu.tcat.account.Account;

class MockAccount implements Account
{

   private final UUID id;
   private final String displayName;

   public MockAccount(UUID id, String displayName)
   {
      this.id = id;
      this.displayName = displayName;

   }

   @Override
   public UUID getId()
   {
      return id;
   }

   @Override
   public String getDisplayName()
   {
      return displayName;
   }

   @Override
   public boolean isActive()
   {
      return true;
   }
}