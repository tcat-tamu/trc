package edu.tamu.tcat.trc.entries.types.biblio.repo;

import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.Work;

public interface WorkRepository
{
   /**
    * @return Edit command to modify and persist a new work document.
    */
   EditWorkCommand createWork();

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
