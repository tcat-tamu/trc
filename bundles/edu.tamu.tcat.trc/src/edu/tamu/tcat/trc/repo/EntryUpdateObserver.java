package edu.tamu.tcat.trc.repo;

/**
 *  An observer that can be registered with the {@link DocumentRepository} to listen to
 *  update action either before or after they are commited to the database layer.
 *
 * @param <StorageType>
 */
public interface EntryUpdateObserver<StorageType>
{
   String notify(UpdateContext<StorageType> context);
}