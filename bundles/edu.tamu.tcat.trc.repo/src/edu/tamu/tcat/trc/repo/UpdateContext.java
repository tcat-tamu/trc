package edu.tamu.tcat.trc.repo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import edu.tamu.tcat.account.Account;

/**
 * Represents contextual information about an in-progress update to a TRC entry.
 * The {@link UpdateContext} will be created when an edit command is executed
 * and provide access to the state of the stored data object prior to the updates.
 * The context will be supplied to the pre- and post-commit hooks. An executable
 * sub-class, {@link ExecutableUpdateContext} will be supplied to the
 * {@link EditCommandFactory} for use in constructing new edit command instances.
 *
 * <p>A given update action is controlled by edit command, and transitions through a
 * specific lifecycle. The states of that lifecycle are defined by the {@link UpdateStatus}
 * enum. Specifically, upon creation, the update is initially in the
 * {@link UpdateStatus#PENDING} state. During the pending phase the application uses the
 * edit command interface to make any changes to the underlying data but no data is saved to
 * the persistence layer. During this phase, the edit command (and corresponding
 * {@code UpdateContext} may be discarded with no impact on the underlying persistence layer
 * (and no need to explicitly free resources). Once the command is executed, the update action
 * scheduled to be run and enters the {@link UpdateStatus#SUBMITTED} stage. Once execution
 * begins it transitions to the {@link UpdateStatus#INPROGRESS} stage. At this point,
 * pre-commit hooks are run and the 'original' value of the record is retrieved from the
 * persistence layer. If the pre-commit hooks fail, the update is aborted and transitions to
 * the {@link UpdateStatus#ERROR} state. Otherwise, the update is executed against the
 * underlying persistence layer and, upon successful completion, transitions to the
 * {@link UpdateStatus#COMPLETED} state. Post-commit hooks are run following the completion
 * of the update. These updates may supply problems, but cannot result in state transitions
 * (e.g., to an error state).
 *
 * <p>Note that some values will not be available at all stages in the update lifecycle.
 * Notably, the 'original' state of the record will only be available once execution has
 * begun. The execution timestamp and modified representation will only be available once the
 * update has been completed.
 *
 * @param <StorageType> The type of the storage record. This should be a simple
 *    JSON serializable struct.
 *
 */
public interface UpdateContext<StorageType>
{
   public enum Severity
   {
      WARNING, ERROR, MESSAGE
   }

   public enum UpdateStatus
   {
      /** Update has been created and is awaiting execution of the associated edit command. */
      PENDING,

      /** Edit command has been executed and scheduled to run against the underlying persistence store. */
      SUBMITTED,

      /** Update is currently being executed by the repository. */
      INPROGRESS,

      /** Update has completed successfully. */
      COMPLETED,

      /** Update has been canceled and was not submitted. */
      CANCELED,

      /** An error prevented the successful exectuion of this update.*/
      ERROR
   }

   /**
    *  A problem reported during the process of updating a document record.
    */
   public interface UpdateProblem
   {
      /**
       * @return The severity of the reported problem
       */
      Severity getSeverity();

      /**
       * @return A message describing the reported problem.
       */
      String getMessage();

      /**
       * @return The exception that led to this problem. May be <code>null</code>.
       */
      Exception getException();
   }

   /**
    *
    * @return The id of the entry being updated.
    * TODO rename to getEntryId
    */
   String getId();

   /**
    * @return A unique identifier for this update.
    */
   UUID getUpdateId();

   /**
    * Returns the timestamp when this update was executed. Only available during the post-commit phase.
    *
    * @return The timestamp when this update was executed.
    */
   Instant getTimestamp();

   /**
    * @return The type of update action. Typically, CREATE, EDIT, REMOVE
    */
   UpdateActionType getActionType();

   /**
    * @return The account of the individual responsible for making these changes.
    */
   Account getActor();

   /**
    * Called by various components responsible for executing the update and pre/post-commit
    * hooks to add information about any errors that are encountered during execution.
    *
    * @param msg An error message
    */
   default void addError(String msg)
   {
      addError(Severity.ERROR, msg, null);
   }

   /**
    * Called by various components responsible for executing the update and pre/post-commit
    * hooks to add information about any errors that are encountered during execution.
    *
    * @param severity The severity of the recorded error
    * @param msg An error message
    * @param ex Optionally an exception that caused in the error
    */
   void addError(Severity severity, String msg, Exception ex);

   /**
    * @return The messages associated with any errors that may have been encountered while
    *    executing this update.
    */
   List<UpdateProblem> listErrors();

   /**
    * @return The state of the entry when the {@link UpdateContext} was created. Note that the
    *    entry may be changed following creation of the {@link UpdateContext} but prior to the
    *    execution of changes to the context.
    */
   Optional<StorageType> getInitialState();

   /**
    * @return The pre-commit representation of the entry prior to being modified. Note that
    *    this may be empty if, for example, the updates apply to a newly created entry. This
    *    instance is loaded during the update execution stage before any other actions are
    *    applied. The returned object MUST NOT be modified by the caller.
    */
   Optional<StorageType> getOriginal();

   /**
    * @return The modified representation of the entry. This is the form of the entry that
    *    will be persisted on successful execution.
    */
   StorageType getModified();
}
