package edu.tamu.tcat.trc.entries.notification;

import java.util.function.Supplier;

public interface UpdateEvent<T> extends Supplier<T>
{
   // TODO need to support fine-grained updates for auditing, provenance and to minimize
   //      impact of concurrent changes.

   /**
    * Possible update action types. Note that this is intended for coarse grained
    * update notifications only.
    */
   public static enum UpdateAction {
      CREATE, UPDATE, DELETE;
   }

   /**
    * @return An identifier for the entity that was updated.
    */
   String getEntityId();

   /**
    * @return The type of update that was performed.
    */
   UpdateAction getAction();

   /**
    * @return A reference to the state of the updated object prior to this update.
    */
   T getOriginal();

   /**
    * @return A reference to the state of the entity immediately following this update.
    */
   @Override
   T get();
}
