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
package edu.tamu.tcat.trc.entries.types.biblio.repo.copies;

import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;

public interface CopyChangeEvent extends UpdateEvent
{
   /**
    * Retrieves the element that was changed. Implementations should attempt to return
    * a copy of the element in the state it was immediately after the change occurred.
    *
    * @return the element that was changed.
    * @throws CatalogRepoException If the element cannot be retrieved. This is expected
    *    in the case of {@link UpdateAction#DELETED} events. In other cases, this is not
    *    expected but due to an internal error.
    */
   /*
    * See the note on RelationshipChangeEvent
    */
   CopyReference get() throws CatalogRepoException;

   CopyReference getOriginal() throws CatalogRepoException;
}
