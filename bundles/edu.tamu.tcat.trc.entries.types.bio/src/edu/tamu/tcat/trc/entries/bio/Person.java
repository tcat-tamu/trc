package edu.tamu.tcat.trc.entries.bio;

import java.util.Set;

import edu.tamu.tcat.catalogentries.events.HistoricalEvent;

/**
 * Represents a historical figure described in the collection. This is typically used to
 * represent a person.
 */
public interface Person
{
   /**
    * @return A unique, persistent identifier for this person.
    */
   String getId();

   /**
    * @return The canonical name associated with this person. Many people are commonly referenced
    *       by multiple names, for example pen names or titles of nobility. This form of the
    *       represents an editorially determined 'canonical' representation of this person.
    * @see Person#getAlternativeNames()
    */
   PersonName getCanonicalName();

   /**
    * @return a set of alternative names for this person.
    * @see #getCanonicalName()
    */
   Set<PersonName> getAlternativeNames();

   /**
    * @return The date of this person's birth. NOTE that this API is provisional and will likely change
    *    either to incorporate the new Java 8 time utilities or to provide more richly structured
    *    information about the person's birth (e.g., including location, fuzzy dates, etc).
    */
   HistoricalEvent getBirth();

   /**
    * @return The date of this person's death. NOTE that this API is provisional and will likely change
    *    either to incorporate the new Java 8 time utilities or to provide more richly structured
    *    information about the person's death (e.g., including location, fuzzy dates, etc).
    */
   HistoricalEvent getDeath();

   /**
    * @return A summary description of this person.
    */
   String getSummary();
}
