package edu.tamu.tcat.trc.refman;

import java.net.URI;
import java.util.function.Consumer;

import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkChangeEvent;
import edu.tamu.tcat.trc.refman.types.ItemType;


public interface ReferenceRepository
{
   // TODO perhaps should not be API. Might be an implementation detail

   BibliographicReference get(ReferenceCollection collection, URI id) throws RefManagerException;

   // TODO should this be required at this stage?
   // TODO define collection membership API
   EditItemCommand create(ReferenceCollection collection, ItemType type) throws RefManagerException;

   EditItemCommand edit(ReferenceCollection collection, URI id) throws RefManagerException;

   void delete(ReferenceCollection collection, URI id) throws RefManagerException;

   AutoCloseable addUpdateListener(Consumer<WorkChangeEvent> ears);

}
