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
