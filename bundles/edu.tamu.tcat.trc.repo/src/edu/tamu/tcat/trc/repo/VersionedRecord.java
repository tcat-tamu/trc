package edu.tamu.tcat.trc.repo;

import java.time.Instant;

import edu.tamu.tcat.account.Account;

public interface VersionedRecord<RecordType>
{
   String getRecordId();

   String getVersionId();

   Instant getModificationDate();

   Account getActor();

   RecordType getRecord();

   // TODO get changes, compute difference, isMostRecent, tag . . .
}
