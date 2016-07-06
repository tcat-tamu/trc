package edu.tamu.tcat.trc.entries.core;

/**
 * A reference to an entry within a TRC repository.
 */
public class EntryReference
{
   /** Defines the type of entry this reference pertains to. */
   public String type;

   /** The unique identifier for a particular entry. */
   public String id;

   /**
    * The version of the entry being referenced. Negative values indicate that
    * no version is specified and target the latest version of the entry. If a
    * non-negative number is provided, it must reference a defined version of
    * the entry. If that version is not defined for the identified entry, this
    * reference is invalid.
    */
   public long version = -1;
}
