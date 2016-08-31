package edu.tamu.tcat.trc.repo;

import java.time.Instant;
import java.util.UUID;

import edu.tamu.tcat.account.Account;

/**
 * Represents contextual information about an in-progress update to a TRC entry.
 * The {@link UpdateContext} will be created when an edit command is executed
 * and provide access to the state of the stored data object prior to the updates.
 * The context will be supplied to the pre and post commit hooks, as well as to the
 * edit command update function.
 *
 * Note that the modified representation of the TRC entry will only be available to the
 * post commit hooks.
 *
 * @param <StorageType> The type of the storage record. This should be a simple
 *    JSON serializable struct.
 *
 */
public interface UpdateContext<StorageType>
{
   /** Defines the class of update action performed. */
   public enum ActionType
   {
      CREATE, EDIT, REMOVE;
   }
//   TODO
//   * Allow pre-commit and post commit actions to log status (make into a monitor
//       and support notification)
//       - include completion/stage status
//   * Include support for progress monitoring
//   * Make log serializable to support post use lookups

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
    * Returns the timestamp when this update was executed. Must only be called in the post commit
    * phase of the action.
    *
    * @return The timestamp when this update was executed.
    */
   Instant getTimestamp();

   /**
    * @return The type of update action. Typically, CREATE, EDIT, REMOVE
    */
   ActionType getActionType();

   /**
    * @return The account of the individual responsible for making these changes.
    */
   Account getActor();

   // TODO return entry reference

   /**
    * @return The pre-commit representation of the entry prior to being modified. Note that
    *    this may be <code>null</code> if, for example, the updates apply to a newly
    *    created entry. This instance is loaded during the update execution stage before
    *    any other actions are applied. The returned object MUST NOT be modified by the caller.
    */
   StorageType getOriginal();

   /**
    * @return The modified representation of the entry. This is the form of the entry that
    *    will be persisted on successful execution.
    */
   StorageType getModified();

}
