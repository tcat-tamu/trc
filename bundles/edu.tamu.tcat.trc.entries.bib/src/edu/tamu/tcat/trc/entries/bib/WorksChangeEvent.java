package edu.tamu.tcat.trc.entries.bib;

public interface WorksChangeEvent
{
   // TODO may factor this into a shared API across catalog services

   enum ChangeType {
      CREATED, MODIFIED, DELETED;
   }

   /**
    * @return The type of change that occurred.
    */
   ChangeType getChangeType();

   /**
    * @return The persistent identifier for the relationship that changed.
    */
   String getWorkId();

   /**
    * Retrieves the work that was changed.
    *
    * @return the work that was changed.
    * @throws WorkNotAvailableException If the work cannot be retrieved (for example,
    *       if the record was deleted).
    */
   Work getWorkEvt() throws WorkNotAvailableException;

}
