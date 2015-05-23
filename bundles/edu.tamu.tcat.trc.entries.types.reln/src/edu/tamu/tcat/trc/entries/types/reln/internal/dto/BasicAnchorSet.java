package edu.tamu.tcat.trc.entries.types.reln.internal.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.AnchorSet;

public class BasicAnchorSet implements AnchorSet
{
   private final Set<Anchor> anchors;

   public BasicAnchorSet(Set<Anchor> anchors)
   {
      this.anchors = new HashSet<>(anchors);
   }

   @Override
   public Collection<Anchor> getAnchors()
   {
      return Collections.unmodifiableCollection(anchors);
   }
}
