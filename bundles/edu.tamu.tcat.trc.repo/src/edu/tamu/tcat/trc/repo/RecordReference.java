package edu.tamu.tcat.trc.repo;

import java.time.Instant;
import java.util.List;

import edu.tamu.tcat.account.Account;

/**
 * A reference to a document record maintained within a document repository. Provides access to
 * information about the record including version history in addition to a method to retrieve
 * the record itself.
 *
 * @param <RecordType> The domain model of the referenced record.
 */
public interface RecordReference<RecordType>
{

   /**
    * @return The unique identifier for the referenced record.
    */
   String getId();

   /**
    * @return Indicates whether this record has been marked as deleted. Note that
    *       deleted records are retained for historical purposes.
    */
   boolean isDeleted();

   /**
    * @return The account that owns or is otherwise responsible for this record.
    */
   Account getOwner();

   /**
    * @return The date this record was initially created.
    */
   Instant getDateCreated();

   /**
    * @return The date this record was last modified.
    */
   Instant getLastModified();

   /**
    * @param filter A filter to constrain the results.
    * @return A list of all versions of this document.
    */
   List<VersionMeta> listVersions(VersionFilter filter);

   /**
    * @param id The id of the version to be retrieved.
    * @return The requested version of this record.
    */
   VersionedRecord<RecordType> getVersion(String versionId);

   /**
    * @return The referenced record.
    */
   RecordType get();
}
