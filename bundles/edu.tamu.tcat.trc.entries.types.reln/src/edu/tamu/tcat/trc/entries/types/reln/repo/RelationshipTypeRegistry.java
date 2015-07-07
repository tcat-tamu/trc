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
package edu.tamu.tcat.trc.entries.types.reln.repo;

import java.util.Set;

import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;

/**
 *  A service to access defined relationship types.
 */
public interface RelationshipTypeRegistry
{
   /**
    * Attempts to find and return the {@link RelationshipType} with the supplied
    * identifier.
    *
    * @param typeIdentifier The unique identifer of the {@code RelationshipType}
    *       to be retrieved.
    * @return The identified {@code RelationshipType}
    * @throws RelationshipException If the identified relationship type has not been
    *       registered with this registry.
    */
   RelationshipType resolve(String typeIdentifier) throws RelationshipException;

   /**
    * @return The identifiers of all currently registered relationship types. Note that
    *       types may be registered or unregistered at any time. Consequently, the
    *       results of a call to this method may be out of date as soon as they are
    *       returned.
    */
   Set<String> list();
}
