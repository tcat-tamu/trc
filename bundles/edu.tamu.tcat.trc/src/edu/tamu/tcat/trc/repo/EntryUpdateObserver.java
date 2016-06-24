package edu.tamu.tcat.trc.repo;

/**
 *  An observer that can be registered with the {@link DocumentRepository} to listen to
 *  update action either before or after they are committed to the database layer.
 *
 * @param <StorageType>
 */
public interface EntryUpdateObserver<StorageType>
{
   /**
    * Called during the pre-commit or post-commit phase of an entry update cycle.
    *
    * @param context The update context for the entry being modified.
    */
   void notify(UpdateContext<StorageType> context);
}