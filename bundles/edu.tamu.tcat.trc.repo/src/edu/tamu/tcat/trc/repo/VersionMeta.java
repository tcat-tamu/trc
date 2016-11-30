package edu.tamu.tcat.trc.repo;

import java.time.Instant;

import edu.tamu.tcat.account.Account;

/**
 *  Basic information about a particular version of a document record.
 */
public interface VersionMeta
{

   /**
    * @return The unique identifier for this version.
    */
   String getVersionId();

   /**
    * @return The unique identifier for this record.
    */
   String getRecordId();

   /**
    * @return The date this version of the record was created.
    */
   Instant getDate();

   /**
    * @return The account responsible for making these changes.
    */
   Account getActor();
}
