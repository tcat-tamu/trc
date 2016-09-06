package edu.tamu.tcat.trc.entries.types.bio.repo;

/**
 * Used to update a HistoricalEvent.
 *
 * @deprecated To be migrated to the HistoricalEvent TRC Entry.
 */
@Deprecated
public interface HistoricalEventMutator
{
   /**
    * @param title The title of this event (for display).
    */
   void setTitle(String title);

   /**
    * @param description A description of this event.
    */
   void setDescription(String description);

   /**
    * @param location The location where this event took place.
    */
   void setLocation(String location);

   /**
    * @return A mutator to be used to edit the date associated with this event.
    */
   DateDescriptionMutator editDate();
}
