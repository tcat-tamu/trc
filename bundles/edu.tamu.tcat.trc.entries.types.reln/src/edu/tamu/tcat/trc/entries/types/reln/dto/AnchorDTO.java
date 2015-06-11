package edu.tamu.tcat.trc.entries.types.reln.dto;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.URIParseHelper;
import edu.tamu.tcat.trc.entries.types.reln.internal.dto.BasicAnchor;

public class AnchorDTO
{
   public Set<String> entryUris = new HashSet<>();

   public AnchorDTO()
   {

   }

   /**
    * @param uris The string-valued URIs for entities to be referenced by this anchor.
    * @throws IllegalArgumentException If one or more of the supplied values are not
    *       syntactically valid URIs. Note that no attempt is made to resolve these URIs
    *       into catalog entries.
    *
    */
   public AnchorDTO(Set<String> uris)
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
   public static AnchorDTO create(Anchor anchor)
   {
      AnchorDTO result = new AnchorDTO();
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
   public static Anchor instantiate(AnchorDTO data)
   {
      return new BasicAnchor(URIParseHelper.parse(data.entryUris));
   }
}
