package edu.tamu.tcat.catalogentries.events;


/**
 * Provides a representation of a historical event that occurs as a specific
 * place and date.
 */
public interface HistoricalEvent
{

   // NOTE This will be extended and revised significantly as we flesh out the notion
   //      of events. Should perhaps be changed to be a more simple identifier and we can
   //      use other controls to attach additional info, but I think, a start date, end date,
   //      location, title and description are probably a good basic description for the

   /**
    * @return A unique, persistent identifier for this event.
    */
   String getId();

   /**
    * @return A title for this event for display purposes.
    */
   String getTitle();

   /**
    * @return A brief description of this event.
    */
   String getDescription();

   /**
    * @return The date at when this event happened. This value will not be null.
    */
   DateDescription getDate();

   /**
    * @return The location where this event happened.
    */
   String getLocation();
}
