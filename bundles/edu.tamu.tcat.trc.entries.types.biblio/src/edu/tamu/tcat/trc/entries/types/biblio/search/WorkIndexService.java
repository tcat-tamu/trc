package edu.tamu.tcat.trc.entries.types.biblio.search;

import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;

/**
 * Responsible for handling work create, update, and delete events and making the appropriate
 * modifications to the search index.
 */
public interface WorkIndexService
{
   /**
    * Notify the index layer of a new work's creation.
    *
    * @param work
    */
   void index(BibliographicEntry work);

   /**
    * Notify the index layer of an updated work
    * TODO: should this use a different "what's changed" scheme?
    *       Might implement this in the future; for now we'll just pass the updated work to "index()",
    *       and the search layer will have to figure out if it is already indexed
    *
    * @param newWork
    * @param oldWork
    */
//   void reindex(Work newWork, Work oldWork);

   /**
    * Notify the index layer of a deleted work.
    *
    * @param id
    */
   void remove(String id);
}
