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
import java.util.Collection;
import java.util.Collections;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;

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