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
package edu.tamu.tcat.trc.entries.types.reln.internal.dto;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import edu.tamu.tcat.trc.entries.types.reln.Provenance;

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