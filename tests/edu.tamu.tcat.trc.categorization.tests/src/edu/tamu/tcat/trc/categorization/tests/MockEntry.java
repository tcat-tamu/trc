package edu.tamu.tcat.trc.categorization.tests;

import java.util.UUID;

class MockEntry
{
   String id;
   private String description;

   public MockEntry(String description)
   {
      this.id = UUID.randomUUID().toString();
      this.description = description;
   }

   public String getId()
   {
      return id;
   }

   public String getDescription()
   {
      return description;
   }
}