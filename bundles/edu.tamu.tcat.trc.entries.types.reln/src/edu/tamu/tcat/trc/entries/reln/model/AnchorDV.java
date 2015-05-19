package edu.tamu.tcat.trc.entries.reln.model;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import edu.tamu.tcat.trc.entries.reln.Anchor;
import edu.tamu.tcat.trc.entries.reln.model.internal.BasicAnchor;

public class AnchorDV
{
   public Set<String> entryUris = new HashSet<>();

   public AnchorDV()
   {

   }

   /**
    * @param uris The string-valued URIs for entities to be referenced by this anchor.
    * @throws IllegalArgumentException If one or more of the supplied values are not
    *       syntactically valid URIs. Note that no attempt is made to resolve these URIs
    *       into catalog entries.
    *
    */
   public AnchorDV(Set<String> uris)
   {
      // validate that the supplied values are valid URIs.
      URIParseHelper.validate(uris);
      entryUris.addAll(uris);
   }

   /**
    * Constructs a data vehicle from an {@link Anchor} instance.
    *
    * @param anchor The anchor for which to construct a data vehicle.
    * @return The instantiated (mutable) data vehicle.
    */
   public static AnchorDV create(Anchor anchor)
   {
      AnchorDV result = new AnchorDV();
      for (URI uri : anchor.getEntryIds())
      {
         result.entryUris.add(uri.toASCIIString());
      }

      return result;
   }

   /**
    * Factory method to create immutable API instances from a data vehicle.
    *
    * @param data The source data from which to create an API instance.
    * @return The created anchor.
    */
   public static Anchor instantiate(AnchorDV data)
   {
      return new BasicAnchor(URIParseHelper.parse(data.entryUris));
   }
}
