package edu.tamu.tcat.trc.repo.postgres.id;

import java.util.UUID;

import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;

/**
 *  For obtaining {@link IdFactory}s that produce UUIDs.
 */
public class UuidFactoryProvider implements IdFactoryProvider
{
   @Override
   public IdFactory getIdFactory(String context)
   {
      return () -> {
         return UUID.randomUUID().toString();
      };
   }

}
