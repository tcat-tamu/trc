package edu.tamu.tcat.trc.entries.types.biblio.search;

import edu.tamu.tcat.trc.entries.types.biblio.Work;

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
   void workCreated(Work work);

   /**
    * Notify the index layer of an updated work
    * TODO: should this use a different "what's changed" scheme?
    *
    * @param newWork
    * @param oldWork
    */
   void workUpdated(Work newWork, Work oldWork);

   /**
    * Notify the index layer of a deleted work.
    *
    * @param id
    */
   void workDeleted(String id);
}
