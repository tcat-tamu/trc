package edu.tamu.tcat.trc.entries.core.resolver;

import static java.text.MessageFormat.format;

/**
 * A reference to an entry within a TRC repository.
 */
public class EntryReference
{

   // TODO this is really and id, not a reference.
   // TODO what about sub-parts, eg. a Volume of a work?

   /** Defines the type of entry this reference pertains to. */
   public String type;

   /** The unique identifier for a particular entry. */
   public String id;

   // TODO support equals, hashcode and to-string

   @Override
   public String toString()
   {
      return format("Entry Reference: \n\ttype={0}\n\tid={1}", type, id);
   }
}
