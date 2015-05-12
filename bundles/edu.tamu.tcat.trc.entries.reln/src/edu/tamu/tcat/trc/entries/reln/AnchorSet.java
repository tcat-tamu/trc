package edu.tamu.tcat.trc.entries.reln;

import java.util.Collection;

/**
 *
 *  <p>
 *  Both {@link Anchor}s and {@code AnchorSet}s provide support for targeting multiple
 *  resources via a {@link Relationship}. The primary difference is that the entries
 *  targeted by an {@code Anchor} are considered to be a single logical entity (as in
 *  the multiple works referenced by the phrase, "there are in the works of Tillotson")
 *  whereas an {@code AnchorSet} is used to group two or more related but distinct works
 *  (as in "Paley rebutted the arguments of both Hume and Spinoza").
 */
public interface AnchorSet
{

   /**
    * @return The anchors that are part of this set.
    */
   Collection<Anchor> getAnchors();
}
