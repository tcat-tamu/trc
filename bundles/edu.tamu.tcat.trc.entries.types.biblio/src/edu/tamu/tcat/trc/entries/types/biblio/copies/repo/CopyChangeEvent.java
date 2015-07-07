package edu.tamu.tcat.trc.entries.types.biblio.copies.repo;

import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;

public interface CopyChangeEvent extends UpdateEvent
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
    * See the note on RelationshipChangeEvent
    */
   CopyReference get() throws CatalogRepoException;

   CopyReference getOriginal() throws CatalogRepoException;
}
