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

import java.util.Set;

import edu.tamu.tcat.trc.resolver.EntryId;

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
    * @return A reference to the entry this anchor is associated with.
    */
   EntryId getTarget();

   /**
    * @return A list of the application-defined properties that have been supplied
    *    for this anchor.
    */
   Set<String> listProperties();

   /**
    * @param property The property whose value should be returned.
    * @return The value supplied for this property
    * @throws IllegalArgumentException If the requested property has not been defined for
    *       this anchor..
    */
   String getProperty(String property);

}
