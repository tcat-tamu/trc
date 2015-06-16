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
