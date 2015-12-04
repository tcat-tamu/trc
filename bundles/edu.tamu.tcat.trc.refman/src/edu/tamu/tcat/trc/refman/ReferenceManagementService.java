package edu.tamu.tcat.trc.refman;

import java.util.Set;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.refman.providers.CollectionProvider;

/**
 * The main entry point for accessessing the reference management service.
 *
 */
public interface ReferenceManagementService
{

   <X extends CollectionProvider> Set<X> listCollectionProviders(Class<X> type);

   <X extends CollectionProvider> X getCollectionProvider(String id, Class<X> type);

   void register(Account account, String authToken, CollectionProvider provider);



}
