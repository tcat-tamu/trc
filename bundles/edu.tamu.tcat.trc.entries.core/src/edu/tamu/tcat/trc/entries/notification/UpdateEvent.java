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
package edu.tamu.tcat.trc.entries.notification;

import java.time.Instant;
import java.util.UUID;

/**
 * A base event type to be used to notify listeners of TRC entry repositories of
 * additions, mutations, and deletions. Subtypes are intended to be used to access
 * details for a repository.
 */
public interface UpdateEvent
{
   /**
    * The type of action for an {@link UpdateEvent}.
    * <p>
    * This set is appropriate as an enum because it is closed. All changes must either
    * be an addition, removal, or mutation, and there is no expectation of another
    * category of mutation.
    */
   enum UpdateAction
   {
      CREATE, UPDATE, DELETE;
   }

   /**
    * @return The type of change that occurred. Not {@code null}
    */
   UpdateAction getUpdateAction();

   /**
    * Get the canonical identifier for the updated entity. If a deletion, the entity
    * referenced by the returned value likely is no longer accessible.
    * <p>
    * This requires that any entity in the system for which an update event may be
    * registered also be uniquely identified via this identifier. If granular updates
    * are made to a larger element, then the event sub-type should have API allowing access
    * to the sub-elements updated.
    *
    * @implNote
    * The String may represent a UUID or URI, depending on what the repo wants. The
    * consumer will need to know to interpret it properly, if necessary. However, the event
    * sub-type will take most of the burden of resolving the entity and its changes from this
    * API, allowing each repository to specialize and provide API to manage sub-entities
    * as well as collections.
    *
    * Using a URI instead of String:
    * The URI should not be the same as what is used to reference the element via REST. This
    * URI is a path understood between the event source and consumer. To allow for changes
    * to sub-entities, the URI becomes a path relative within the repository to the entity
    * changed. However, the URI must then be parsed to determine the exact scope of the change
    * rather than using a subclass API to help determine it. If a subclass is available, just
    * use it to determine the change scope.
    * One problem with this approach is management of collections, such as a list or set
    * of items that are not individually identified, perhaps only by ordinal. If each has
    * a unique identifier, they may as well use that String as a shorter form.
    *
    * Using a UUID instead of String:
    * Requires each element that can send changes to be uniquely identified, but still
    * does not solve the problem of set or list ordinal or collection mutations.
    * Also, the UUID of sub-elements may not be resolved easily within the repository,
    * creating the burden of maintaining a large UUID to entity index
    */
   String getEntityId();

   /**
    * Get the identifier of the account that was the instigator of the change. May be a
    * user account or internal system account.
    */
   UUID getActor();

   /**
    * Get the time the change was made. This is useful for logging purposes as well
    * as for subsequent changes to be allowed to occur at the "same time".
    */
   Instant getTimestamp();
}
