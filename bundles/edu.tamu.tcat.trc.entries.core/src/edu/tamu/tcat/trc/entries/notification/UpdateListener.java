package edu.tamu.tcat.trc.entries.notification;

/**
 * Notified of updates to catalog entries.
 *
 * @param <T>
 */
public interface UpdateListener<T>
{

   /**
    * Called prior to any updates to a catalog entry. Listeners can elect to cancel an update
    * by returning {@code false} from this method. Implementations should reflect the fact
    * 'before update' notifications may not actually result in the update action being
    * completed (if another listener cancels the action). Update actions will block while the
    * {@link #beforeUpdate(UpdateEvent)} method is called for each registered listener.
    * Consequently, implementations should be return as quicly as possible to prevent bottlenecks.
    *
    * <p>Implementations are expected to trap and report all exceptions.
    *
    * @param evt The update event that is about to be executed.
    * @return {@code true} if the update should proceed, {@code false} if the it should be
    *       canceled. If any registered listener returns {@code false} for an update, the
    *       update will be canceled.
    */
   boolean beforeUpdate(UpdateEvent<T> evt);

   /**
    * Called after an update to the persistence layer has been completed. This method will be
    * invoked asynchronously.
    *
    * <p>Implementations are expected to trap and report all exceptions.
    *
    * @param evt The update event.
    * @return
    */
   void afterUpdate(UpdateEvent<T> evt);
}
