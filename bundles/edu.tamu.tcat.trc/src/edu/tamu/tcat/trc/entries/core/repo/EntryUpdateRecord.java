package edu.tamu.tcat.trc.entries.core.repo;

import java.time.Instant;
import java.util.UUID;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.resolver.EntryId;

public interface EntryUpdateRecord<EntryType>
{
   enum UpdateAction
   {
      CREATE, UPDATE, REMOVE
   }

   /**
    * @return A unique identifier for this particular update.
    */
   UUID getId();

   /**
    * @return The time in which this update was executed.
    */
   Instant getTimestamp();

   /**
    * @return The type of action performed by this update.
    */
   UpdateAction getAction();

   /**
    * @return The account responsible for performing this action. May be {@code null}
    *       depending on whether the repository has been implemented to allow anonymous
    *       access and modification.
    */
   Account getActor();

   /**
    * @return The reference that uniquely identifies the updated entry.
    */
   EntryId getEntryReference();

   /**
    * @return The state of the entry immediately following this modification. Will be
    *    {@code null} if the entry was deleted.
    */
   EntryType getModifiedState();

   /**
    * @return The state of the entry immediately before this modification. Will be
    *    {@code null} if the entry was created.
    */
   EntryType getOriginalState();
}
