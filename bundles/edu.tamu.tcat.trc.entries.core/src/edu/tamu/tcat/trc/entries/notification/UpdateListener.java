package edu.tamu.tcat.trc.entries.notification;

/**
 * Notified of updates to catalog entries.
 */
public interface UpdateListener<EventType extends UpdateEvent>
{
   /**
    * Called after an update to a catalog entry in a repository.
    * This method may be invoked asynchronously. If any runtime exception
    * is thrown, it will not impact other repository reads, writes, or
    * listener notifications.
    *
    * @param evt The update event representing the change that occurred.
    */
   void handle(EventType evt);
}
