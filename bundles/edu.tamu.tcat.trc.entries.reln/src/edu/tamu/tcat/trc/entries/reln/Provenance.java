package edu.tamu.tcat.trc.entries.reln;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;

/**
 *  Information about the people responsible for the intellectual content of a relationship.
 */
public interface Provenance
{

//   * Note that this may be expanded to include information about any software used or
//   * relationships that were generated through automated processes.

   /**
    * Returns a collection of URIs that references the people or other entities responsible
    * for the intellectual content of an annotation. Note that how the selection of an
    * appropriate URI to reference the creator is left as a design decision of the application
    * that implements the relationship framework. Common choices include the creator's email
    * address, a web page, a URL for a public account profile, or an internal account identifier.
    *
    * <p>In general, this URI should be selected so that the application can uniquely identify
    * creators and easily retrieve annotations created by a particular individual.
    *
    * @return A collection of URIs for the creators of this annotation.
    */
   Collection<URI> getCreators();

   /**
    * @return The date this annotation was first created.
    */
   Instant getDateCreated();

   /**
    * @return The date this annotation was last modified.
    */
   Instant getDateModified();
}
