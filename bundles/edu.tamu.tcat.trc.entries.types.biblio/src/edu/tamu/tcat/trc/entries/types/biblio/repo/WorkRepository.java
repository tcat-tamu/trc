package edu.tamu.tcat.trc.entries.types.biblio.repo;

import java.util.Iterator;

import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.Work;

public interface WorkRepository
{
   /** The type id used to identify bibliographic entries within the EntryResolver framework. */
   public final static String ENTRY_TYPE_ID = "trc.entries.bibliographic";

   /** The initial path (relative to some API endpoint) for building URIs that reference
    *  a bibliographic entry and its sub-elements. */
   public final static String ENTRY_URI_BASE = "entries/bibliographic";

   /**
    * @return An iterator over all works in the repository.
    */
   Iterator<Work> getAllWorks();

   /**
    * @return Edit command to modify and persist a new work document.
    */
   EditWorkCommand createWork();

   /**
    * Creates a work with the given ID. The burden is placed on the implementing code to prevent the
    * creation of works with duplicate IDs.
    *
    * @param id
    * @return Edit command to modify and persist a new work document.
    */
   EditWorkCommand createWork(String workId);

   /**
    * Retrieve an existing work document.
    *
    * @param workId
    * @return
    * @throws IllegalArgumentException if a work with the given ID cannot be found
    */
   Work getWork(String workId);

   /**
    * Edit an existing work document.
    *
    * @param workId ID of desired work instance.
    * @return Edit command to modify work with given ID.
    * @throws IllegalArgumentException if a work with the given ID cannot be found
    */
   EditWorkCommand editWork(String workId);

   /**
    * Delete a work document
    *
    * @param workId
    * @throws IllegalArgumentException if a work with the given ID cannot be found
    */
   void deleteWork(String workId);

   /**
    * Shorthand to retrieve an edition from a work.
    *
    * @param workId
    * @param editionId
    * @return
    * @throws IllegalArgumentException if a work/edition with the given IDs cannot be found
    */
   Edition getEdition(String workId, String editionId);

   /**
    * Shorthand to retrieve a volume from an edition.
    *
    * @param workId
    * @param editionId
    * @param volumeId
    * @return
    * @throws IllegalArgumentException if a work/edition/volume with the given IDs cannot be found
    */
   Volume getVolume(String workId, String editionId, String volumeId);
}
