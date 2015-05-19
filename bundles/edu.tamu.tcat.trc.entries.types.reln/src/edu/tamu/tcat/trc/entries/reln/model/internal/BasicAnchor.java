package edu.tamu.tcat.trc.entries.reln.model.internal;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import edu.tamu.tcat.trc.entries.reln.Anchor;

/**
 *  Simple, immutable implementation of the {@link Anchor} API.
 */
public final class BasicAnchor implements Anchor
{
   private Collection<URI> uris;

   public BasicAnchor(Collection<URI> uris)
   {
      this.uris = uris;
   }

   @Override
   public Collection<URI> getEntryIds()
   {
      return Collections.unmodifiableCollection(this.uris);
   }
}