package edu.tamu.tcat.trc.entries.reln.model.internal;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import edu.tamu.tcat.trc.entries.reln.Provenance;

public class BasicProvenance implements Provenance
{
   private final Collection<URI> creators;
   private final Instant created;
   private final Instant modified;

   public BasicProvenance()
   {
      creators = new HashSet<>();
      created = Instant.now();
      modified = Instant.now();
   }

   public BasicProvenance(Collection<URI> creators, Instant created, Instant modified)
   {
      this.creators = creators;
      this.created = created;
      this.modified = modified;
   }

   @Override
   public Collection<URI> getCreators()
   {
      return Collections.unmodifiableCollection(creators);
   }

   @Override
   public Instant getDateCreated()
   {
      return created;
   }

   @Override
   public Instant getDateModified()
   {
      return modified;
   }
}