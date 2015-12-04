package edu.tamu.tcat.trc.refman.providers;

import java.util.Set;

import edu.tamu.tcat.trc.refman.CollectionDescriptor;
import edu.tamu.tcat.trc.refman.ReferenceCollection;
import edu.tamu.tcat.trc.refman.types.ItemTypeProvider;

/**
 * Defines a connection to an underlying reference management system such as Zotero or a
 * local database.
 *
 * <p>
 * Account access is guarded by an implementation-defined bearer token. The implementation
 * must provide facilities for authorizing users and generating an appropriate bearer token.
 * This token must contain all information required to uniquely and securely identify the
 * user and user permissions associated with a collection. As part of the account
 * establishment/management process, clients must be implemented to support specific collection
 * provider implementations in order to create the appropriate authentication tokens and
 * associate those tokens with a user account. Once this initial linkage has been established,
 * the implementation-dependent account
 */
public interface CollectionProvider
{
   // TODO this is a stub API that currently serves as a placeholder for WIP design and documentation

   /**
    * @return A system
    */
   String getId();

   ItemTypeProvider getTypeProvider();

   Set<CollectionDescriptor> listCollections(String authToken);

   ReferenceCollection get(String authToken, CollectionDescriptor collectionId);

}
