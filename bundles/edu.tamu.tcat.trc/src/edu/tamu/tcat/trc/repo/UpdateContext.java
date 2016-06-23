package edu.tamu.tcat.trc.repo;

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
   /**
    *
    * @return The id of the entry being updated.
    */
   String getId();

   /**
    * @return The type of update action. Typically, CREATE, EDIT, REMOVE
    */
   String getActionType();

   /**
    * @return The account of the individual responsible for making these changes.
    */
   Account getActor();

   // TODO return entry reference

   /**
    * @return The original representation of the entry prior to being modified. This is
    *    loaded during update execution and must not be modified by the caller.
    */
   StorageType getOriginal();

   /**
    * @return The modified representation of the entry. This is the form of the entry that
    *    will be persisted on successful execution.
    */
   StorageType getModified();

}
