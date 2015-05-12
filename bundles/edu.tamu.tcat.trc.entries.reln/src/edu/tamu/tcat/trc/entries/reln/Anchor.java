package edu.tamu.tcat.trc.entries.reln;

import java.net.URI;
import java.util.Collection;

/**
 *  Defines a reference to a catalog entry, a fragment of a catalog entry (for example, to a
 *  discontinuous range of pages within an edition), or to a grouping of catalog entries or
 *  fragments (for example, editions three and four of a work). {@code Anchor}s serve as a
 *  standoff-markup equivalent to embedded anchor elements within HTML documents. By virtue
 *  of adopting a standoff approach to defining {@link Relationship}s and {@code Anchor}s,
 *  this system is capable of representing a more complex link topology than standard HTML.
 *
 *  <p>
 *  For relatively straightforward references to internal structure of catalog entries,
 *  media fragments should be used (see {@link http://www.w3.org/TR/media-frags/}). For more
 *  complex requirements, concrete sub-classes may provide enhanced capabilities to support
 *  specific types of structured anchors or to more effectively support references to internal
 *  structure.
 */
public interface Anchor
{
   /**
    * @return A collection of identifiers for the referenced catalog entries.
    */
   Collection<URI> getEntryIds();

}
