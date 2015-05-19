package edu.tamu.tcat.trc.entries.notification;


/**
 * Supports fine-grained listening for data update actions. The observer will be called when
 * the update starts processing, finishes, is aborted or encounters an error.
 *
 * <p>A simple implementation has been provide in the {@link DataUpdateObserverAdapter}. This
 * correctly handles state changes and supplies event notification methods that can be
 * overwritten by implementations to take action as needed. In most cases, clients should
 * extend this adapter rather than implementing the {@code DataUpdateObserver} interface directly.
 *
 * <p>
 * Implementations should be thread-safe.
 *
 * @param <R>
 */
public interface DataUpdateObserver<R>
{
   // NOTE initially, this was intended to be supplied with the object being updated to monitor
   //      the progress of a single object. In the current incarnation, it is being used
   //      internally to monitor tasks sent to the sql executor via an ObservableTaskWrapper.
   //      This may be an appropriate use, but we may need to re-assess the design.

   enum State {
      PENDING, STARTED, COMPLETED, ABORTED, ERROR, CANCELLED
   }

   /**
    * Indicates that the request associated with this observer is about to start.
    * Note that this will be called in the same thread as the request and will block
    * execution until it completes. If this method returns {@code false} or throws a
    * runtime exception, the request will be aborted and {@link #onAborted(Object)}
    * will be called.
    *
    * <p>
    * Data update operations must check {@link #isCanceled()} prior to starting the
    * operation. If {@code #isCanceled()} returns {@code true}, they should call
    * {@link #onAborted(Object)} without calling {@code #onStart(Object)}.
    *
    * @return {@code false} if the request should be aborted.
    */
   boolean start();

   /**
    * Called upon successful completion of the update task. This allows the caller to
    * perform any required cleanup or to notify upstream callers that the data has been
    * updated.
    *
    * <p>
    * After a call to this method {@link #isCompleted()} will return true and no
    * further calls to state change methods will be allowed.
    *
    * @param result An object that represents the updated data.
    */
   void finish(R result);

   /**
    * Called by the store when a cancelled update task has been terminated.
    *
    * <p>
    * After a call to this method {@link #isCompleted()} will return true and no
    * further calls to state change methods will be allowed.
    */
   void aborted();

   /**
    * Called in the event of an error handling the update.
    *
    * <p>
    * After a call to this method {@link #isCompleted()} will return true and no
    * further calls to state change methods will be allowed.
    *
    * @param message A message describing the error.
    * @param ex The exception resulting from the error. May be {@code null} if no
    *       exception was thrown.
    */
   void error(String message, Exception ex);

   // TODO add monitor method that can be called with progress updates
   // TODO add timeout
   // TODO crosswalk with Future

   /**
    * Indicates whether this request has been cancelled by the client. Data store
    * implementations that process requests are responsible to check this field prior to
    * starting a data update operation. For any long running tasks should check
    * periodically throughout the operation and take appropriate action to cancel the
    * request if possible.
    *
    * @return {@code true} if this request has been cancelled by the client.
    */
   boolean isCanceled();

   /**
    * Indicates whether the action being observed has been completed. This should return
    * true if and only if {@link #finish(Object)}, {@link #onAborted(Object)} or
    * {@link #onError(Object, String, Exception)} have been called.
    *
    * @return {@code true} once one of the three terminal methods have been invoked.
    */
   boolean isCompleted();

   /**
    * @return The current state of this request.
    */
   State getState();
}
