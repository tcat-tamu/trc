package edu.tamu.tcat.trc.resolver;

import static java.text.MessageFormat.format;

import java.net.URI;

import edu.tamu.tcat.trc.TrcException;

/**
 * Thrown when an attempt to resolve an {@link EntryId} fails or the
 * reference is otherwise determined to be invalid.
 */
public class InvalidReferenceException extends TrcException
{
   private static final String ERR_BAD_REFERENCE = "The supplied entry reference '{0}' could not be resolved: {2}";
   private static final String ERR_BAD_ENTRY = "The supplied entry [{0}] cannot be resolved: {1}";
   private static final String ERR_BAD_URI = "The supplied entry uri [{0}] cannot be resolved: {1}";

   private final String msg;
   private final EntryId ref;
   private final Object entry;

   public InvalidReferenceException(EntryId ref, String msg)
   {
      super(format(ERR_BAD_REFERENCE, ref.id, msg));

      this.msg = msg;
      this.ref = ref;
      this.entry = null;
   }

   public InvalidReferenceException(String id, String type, String msg)
   {
      super(format(ERR_BAD_REFERENCE, id, msg));

      this.msg = msg;
      this.ref = null;
      this.entry = null;
   }

   public InvalidReferenceException(Object entry, String msg)
   {
      super(format(ERR_BAD_ENTRY, entry, msg));

      this.msg = msg;
      this.ref = null;
      this.entry = entry;
   }

   public InvalidReferenceException(URI uri, String msg)
   {
      super(format(ERR_BAD_URI, uri, msg));

      this.msg = msg;
      this.ref = null;
      this.entry = null;
   }

   /**
    * @return The message supplied when this reference was determined to be invalid.
    */
   public String getResolverMessage()
   {
      return msg;
   }

   /**
    * @return The invalid reference. May be {@code null}
    */
   public EntryId getEntryReference()
   {
      return ref;
   }

   /**
    * @return The entry that was to be referenced. May be {@code null}
    */
   public Object getEntry()
   {
      return entry;
   }
}
