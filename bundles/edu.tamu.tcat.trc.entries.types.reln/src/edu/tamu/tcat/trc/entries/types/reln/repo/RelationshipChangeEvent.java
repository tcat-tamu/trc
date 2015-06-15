package edu.tamu.tcat.trc.entries.types.reln.repo;

import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;

public interface RelationshipChangeEvent extends UpdateEvent
{
   /**
    * Retrieves the element that was changed. Implementations should attempt to return
    * a copy of the element in the state it was immediately after the change occurred.
    *
    * @return the element that was changed.
    * @throws CatalogRepoException If the element cannot be retrieved. This is expected
    *    in the case of {@link UpdateAction#DELETED} events. In other cases, this is not
    *    expected but due to an internal error.
    */
   /*
    * This API is problematic.
    *    For deletions, this is not available or may be the last state before removal, though
    *    it may be expensive to retain and pass around if nothing actually needs it.
    *    For additions, the entire element should be captured to prevent a subsequent lookup
    *    by a listener from getting a newer state than what it was created as.
    *    For mutations, the state of the element just after mutation should be captured though
    *    it would be less data to only store the properties that changed. If one of these
    *    "properties" carries the entire object, the creation case may be reduced to it.
    */
   Relationship getRelationship() throws CatalogRepoException;
}
