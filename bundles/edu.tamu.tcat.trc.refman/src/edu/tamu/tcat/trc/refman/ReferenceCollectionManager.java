package edu.tamu.tcat.trc.refman;

import java.net.URI;
import java.util.Set;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.refman.types.ItemTypeProvider;

public interface ReferenceCollectionManager
{
   // manages the various accounts visible to a particular account


   // v1 of this API will be minimalist. Eventually this will provide facilities to discover
   // different collection providers and to
   //
   // We will need to support two key tasks.
   //    1. Manage different facilities for accessing reference collections (e.g.,
   //       local DB, Zotero, Mendelly, etc).
   //    2. Provide account-specific access to create/access/manage reference collections





  // discover and access various reference collections on behalf of a user account.

   /**
    * @return A unique identifier for this manager.
    */
   String getId();

   /**
    * @return A title for display.
    */
   String getTitle();

   ReferenceCollection get(Account account, URI id) throws RefManagerException;
   // TODO need to supply authenticated account info


   // TODO how do I get an ItemTypeProvider back when I need it?
   ReferenceCollection create(Account account, String name, ItemTypeProvider provider) throws RefManagerException;


   Set<String> listTypeProviders();



   // TODO need to be able to register repo services and item type providers
   // TODO list all?
}
