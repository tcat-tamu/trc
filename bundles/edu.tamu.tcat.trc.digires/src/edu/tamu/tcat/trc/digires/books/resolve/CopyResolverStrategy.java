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
package edu.tamu.tcat.trc.digires.books.resolve;


/**
 *  Responsible for resolving detailed information about a specific digital copy.
 *
 *  @param T
 */
public interface CopyResolverStrategy<T extends DigitalCopy>
{
   // NOTE this is expected to be used as a concrete sub-type. That is, clients will request and
   //      use the HathiTrustCopyResolver. The returned types provide information that is specific
   //      to their internal representation.

   // TODO what is this? Mediator, Strategy, Facade?
   // TODO provide authorization support


   /**
    * Indicates whether this strategy can resolve copies for the supplied identifier.
    *
    * @param identifier The unique identifier for the copy.
    * @return {@code true} If this copy provide recognizes and can attempt to resolve
    *    the supplied copy.
    */
   boolean canResolve(String identifier);

   /**
    * Retrieves detailed information about a specific digital copy.
    *
    * @param identifier An identifier for the digital copy to return.
    * @return The requested digital copy. Will not be {@code null}.
    *
    * @throws ResourceAccessException If the requested copy could not be found or accessed.
    * @throws IllegalArgumentException If the supplied identifier cannot be interpreted by
    *       this {@code CopyResolverStrategy}.
    */
   T resolve(String identifier) throws ResourceAccessException, IllegalArgumentException;



}
