package edu.tamu.tcat.trc.entries.notification;

import java.util.function.Supplier;

/**
 * A simple implementation of {@link UpdateEvent}.
 *
 * @param <T>
 */
public class BasicUpdateEvent<T> implements UpdateEvent<T>
{
   private final String id;
   private final UpdateAction action;
   private final Supplier<T> origRef;
   private final Supplier<T> currRef;

   private T original;
   private T current;

   public BasicUpdateEvent(String id,
                           UpdateAction action,
                           Supplier<T> origSupplier,
                           Supplier<T> currSupplier)
   {
      this.action = UpdateAction.DELETE;
      this.id = id.toString();
      this.origRef = origSupplier;
      this.currRef = origSupplier;
   }

   @Override
   public String getEntityId()
   {
      return this.id;
   }

   @Override
   public UpdateAction getAction()
   {
      return action;
   }

   @Override
   public synchronized T getOriginal()
   {
      if (original == null)
         original = origRef.get();
      return original;
   }

   @Override
   public T get()
   {
      if (current == null)
         current = currRef.get();
      return current;
   }
}