package edu.tamu.tcat.trc.persist.postgres.id;

import java.util.UUID;

import edu.tamu.tcat.trc.persist.IdFactory;

public class UuidProvider implements IdFactory
{
   @Override
   public String getNextId(String context)
   {
      return UUID.randomUUID().toString();
   }
}
