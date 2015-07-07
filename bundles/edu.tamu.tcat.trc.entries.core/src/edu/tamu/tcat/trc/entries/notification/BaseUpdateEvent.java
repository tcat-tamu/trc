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
import java.util.Objects;
import java.util.UUID;

/**
 * A base implementation of {@link UpdateEvent}, intended to be used
 * as a base class for subtypes.
 */
public class BaseUpdateEvent implements UpdateEvent
{
   protected final String id;
   protected final UpdateAction action;
   protected final UUID actor;
   protected final Instant ts;

   public BaseUpdateEvent(String id,
                          UpdateAction action,
                          UUID actor,
                          Instant ts)
   {
      this.actor = Objects.requireNonNull(actor);
      this.ts = Objects.requireNonNull(ts);
      this.action = Objects.requireNonNull(action);
      this.id = Objects.requireNonNull(id);
   }

   @Override
   public String getEntityId()
   {
      return this.id;
   }

   @Override
   public UpdateAction getUpdateAction()
   {
      return action;
   }

   @Override
   public UUID getActor()
   {
      return actor;
   }

   @Override
   public Instant getTimestamp()
   {
      return ts;
   }

   @Override
   public String toString()
   {
      return "a<" + action + ">id<" + id+">t<"+ts+">acct<"+actor+">";
   }
}
