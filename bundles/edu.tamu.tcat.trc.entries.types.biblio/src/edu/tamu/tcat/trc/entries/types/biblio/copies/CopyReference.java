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
package edu.tamu.tcat.trc.entries.types.biblio.copies;

import java.net.URI;
import java.util.UUID;

import edu.tamu.tcat.trc.entries.types.biblio.Work;

/**
 * Associates a {@link DigitalCopy} with a bibliographic {@link Work} or portion of a work
 * such as an edition or volume.
 */
public interface CopyReference
{
   /**
    * @return A unique, persistent identifier for this reference.
    */
   UUID getId();

   /**
    * @return The URI of the associated bibliographic entry. Note that digital copies may be
    *       attached to works, editions or volumes.
    */
   URI getAssociatedEntry();

   /**
    * @return The unique identifier for the associated digital copy. See
    *       {@link CopyResolverRegistry} for more detail on how to retrieve a reference to
    *       the digital copy.
    */
   String getCopyId();  // TODO need info about provider, type, etc.

   /**
    * @return A title that describes this relationship between the work and the digital copy.
    *       Examples would include 'Black and White', 'High Resolution Color Scan',
    *       'Harvard Copy'. Will not be {@code null}, may be empty string.
    */
   String getTitle();

   /**
    * @return A short description (if desired) that describes interesting features of the
    *       linked copy to aid users in understanding its relevance to their reading. For
    *       example, this might be used to note missing pages, significant annotations or
    *       provenance, or the accuracy of OCR.
    */
   String getSummary();

   /**
    * @return A description of the usage rights of this work.
    */
   String getRights();     // TODO use structured model for rights.
}
