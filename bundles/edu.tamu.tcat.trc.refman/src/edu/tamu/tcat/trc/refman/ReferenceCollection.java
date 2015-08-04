package edu.tamu.tcat.trc.refman;

import java.net.URI;
import java.util.UUID;

import edu.tamu.tcat.trc.refman.search.BibItemSearchCommand;
import edu.tamu.tcat.trc.refman.types.ItemType;
import edu.tamu.tcat.trc.refman.types.ItemTypeProvider;

/**
 * Defines a collection of bibliographic references. This is the primary interface for
 * creating, retrieving and editing bibliographic references.
 *
 *
 */
public interface ReferenceCollection
{
  /*
   *  -- Each bibliographic reference belongs to exactly one collection.
   *  -- instances encapsulate user account information
   *  -- intended to be transient, rather than system services
   *  -- facade over repo, search and other services
   */


   /**
    * @return A unique identifier for this collection.
    */
   UUID getId();

   String getName();

   ItemTypeProvider getTypeProvider();

   BibItemSearchCommand createSearchCommand();

   BibliographicReference get(URI id) throws RefManagerException;

   // TODO should this be required at this stage?
   EditItemCommand create(ItemType type) throws RefManagerException;

   EditItemCommand edit(URI id) throws RefManagerException;

   void delete(URI id) throws RefManagerException;




}
