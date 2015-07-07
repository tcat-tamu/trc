/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.entries.types.reln;

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
