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

import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.repo.RepositoryException;

public interface RelationshipChangeEvent extends UpdateEvent
{

   /**
    * Retrieves the element that was changed. Implementations should attempt to return
    * a copy of the element in the state it was immediately after the change occurred.
    *
    * @return the element that was changed.
    * @throws RepositoryException If the element cannot be retrieved. This is expected
    *    in the case of {@link UpdateAction#DELETED} events. In other cases, this is not
    *    expected but due to an internal error.
    * @deprecated This API is problematic.
    *    For deletions, this is not available or may be the last state before removal, though
    *    it may be expensive to retain and pass around if nothing actually needs it.
    *    For additions, the entire element should be captured to prevent a subsequent lookup
    *    by a listener from getting a newer state than what it was created as.
    *    For mutations, the state of the element just after mutation should be captured though
    *    it would be less data to only store the properties that changed. If one of these
    *    "properties" carries the entire object, the creation case may be reduced to it.
    */
   @Deprecated
   default Relationship getRelationship() throws RepositoryException {
      return null;
   }

}
