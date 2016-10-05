package edu.tamu.tcat.trc.repo.id;

import java.util.UUID;

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
