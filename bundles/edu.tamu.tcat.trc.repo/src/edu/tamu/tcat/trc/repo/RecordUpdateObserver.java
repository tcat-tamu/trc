package edu.tamu.tcat.trc.repo;

import java.util.function.Consumer;

/**
 *  An observer that can be registered with the {@link DocumentRepository} to listen to
 *  update action either before or after they are committed to the database layer.
 *
 * @param <StorageType>
 */
@FunctionalInterface
public interface RecordUpdateObserver<RecordType> extends Consumer<RecordUpdateEvent<RecordType>>
{
   /**
    * @param event An event object that provides information about an updated record.
    */
   @Override
   void accept(RecordUpdateEvent<RecordType> event);
}