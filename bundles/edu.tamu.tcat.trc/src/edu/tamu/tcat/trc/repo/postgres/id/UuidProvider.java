package edu.tamu.tcat.trc.repo.postgres.id;

import java.util.UUID;

import edu.tamu.tcat.trc.repo.IdFactory;

public class UuidProvider implements IdFactory
{
   @Override
   public String getNextId(String context)
   {
      return UUID.randomUUID().toString();
   }
}