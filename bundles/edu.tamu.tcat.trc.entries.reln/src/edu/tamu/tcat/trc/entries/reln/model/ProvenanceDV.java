package edu.tamu.tcat.trc.entries.reln.model;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Set;

import edu.tamu.tcat.trc.entries.reln.Provenance;
import edu.tamu.tcat.trc.entries.reln.model.internal.BasicProvenance;

public class ProvenanceDV
{
   private static final DateTimeFormatter iso8601Formatter = DateTimeFormatter.ISO_INSTANT;

   /** The string-valued URIs associated with the creators of the associated annotation. */
   public Set<String> creatorUris;

   /** Date created in ISO 8601 format such as '2011-12-03T10:15:30Z' */
   public String dateCreated;

   /** Date modified in ISO 8601 format such as '2011-12-03T10:15:30Z' */
   public String dateModified;

   public static ProvenanceDV create(Provenance prov)
   {
      ProvenanceDV result = new ProvenanceDV();
      Instant created = prov.getDateCreated();
      result.dateCreated = (created != null) ? iso8601Formatter.format(created) : null;

      Instant modified = prov.getDateModified();
      result.dateModified = (modified != null) ? iso8601Formatter.format(modified) : null;

      result.creatorUris = URIParseHelper.toStringSet(prov.getCreators());

      return result;
   }

   /**
    * Note that if either the creation or modification date of the supplied PovenanceDV is
    * {@code null}, the corresponding property of the resulting {@link Provenance} will be
    * initialized to the current instant.
    *
    * @param data
    * @return
    */
   public static Provenance instantiate(ProvenanceDV data)
   {
      // TODO handle format errors
      Instant created = (data.dateCreated != null) ? Instant.parse(data.dateCreated) : Instant.now();
      Instant modified = (data.dateModified != null) ? Instant.parse(data.dateModified) : Instant.now();
      Collection<URI> creators = URIParseHelper.parse(data.creatorUris);

      return new BasicProvenance(creators, created, modified);
   }
}
