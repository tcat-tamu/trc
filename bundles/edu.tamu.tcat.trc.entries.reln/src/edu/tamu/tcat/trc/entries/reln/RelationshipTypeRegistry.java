package edu.tamu.tcat.trc.entries.reln;

import java.util.Set;

/**
 *  A service to access defined relationship types.
 */
public interface RelationshipTypeRegistry
{
   /**
    * Attempts to find and return the {@link RelationshipType} with the supplied
    * identifier.
    *
    * @param typeIdentifier The unique identifer of the {@code RelationshipType}
    *       to be retrieved.
    * @return The identified {@code RelationshipType}
    * @throws RelationshipException If the identified relationship type has not been
    *       registered with this registry.
    */
   RelationshipType resolve(String typeIdentifier) throws RelationshipException;

   /**
    * @return The identifiers of all currently registered relationship types. Note that
    *       types may be registered or unregistered at any time. Consequently, the
    *       results of a call to this method may be out of date as soon as they are
    *       returned.
    */
   Set<String> list();
}
