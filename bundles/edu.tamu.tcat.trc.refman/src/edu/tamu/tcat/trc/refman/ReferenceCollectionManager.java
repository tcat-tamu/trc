package edu.tamu.tcat.trc.refman;

import java.util.UUID;

import edu.tamu.tcat.trc.refman.types.ItemTypeProvider;

public interface ReferenceCollectionManager
{
  // discover and access various reference collections on behalf of a user account.

   ReferenceCollection get(UUID id);   // TODO need to supply authenticated account info


   // TODO how do I get an ItemTypeProvider back when I need it?
   ReferenceCollection create(ItemTypeProvider typeProvider, String name);

   // TODO need to be able to register repo services and item type providers
   // TODO list all?
}
