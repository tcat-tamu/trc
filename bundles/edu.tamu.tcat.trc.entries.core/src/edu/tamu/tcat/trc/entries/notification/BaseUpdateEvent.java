package edu.tamu.tcat.trc.entries.notification;

import java.util.Objects;

/**
 * A base implementation of {@link UpdateEvent}, intended to be used
 * as a base class for subtypes.
 */
public class BaseUpdateEvent implements UpdateEvent
{
   protected final String id;
   protected final UpdateAction action;

   public BaseUpdateEvent(String id, UpdateAction action)
   {
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
   public String toString()
   {
      return "a<" + action + ">id<" + id+">";
   }
}
