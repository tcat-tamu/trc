package edu.tamu.tcat.trc.repo;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import edu.tamu.tcat.account.Account;

/**
 * Event supplied to observers upon updates to a record.
 *
 * @param <RecordType> The type of the update record.
 *
 */
public interface RecordUpdateEvent<RecordType>
{
   /**
    * @return The id of the updated record.
    */
   String getRecordId();

   /**
    * @return A unique identifier for this update.
    */
   UUID getUpdateId();

   /**
    * @return The timestamp when this update was executed.
    */
   Instant getTimestamp();

   /**
    * @return The type of update.
    */
   UpdateActionType getUpdateType();

   /**
    * @return The account of the individual responsible for making these changes.
    */
   Account getActor();

   /**
    * @return The original representation of the entry immediately prior to the update.
    */
   Optional<RecordType> getOriginalRecord();

   /**
    * @return The updated representation of the entry. Note that this reflects the state
    *    of the record at the time of this update and will not incorporate any subsequent
    *    changes.
    */
   Optional<RecordType> getUpdatedRecord();
}
