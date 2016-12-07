package edu.tamu.tcat.trc.repo.postgres;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.QueryBuilder;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.repo.RecordReference;
import edu.tamu.tcat.trc.repo.VersionFilter;
import edu.tamu.tcat.trc.repo.VersionMeta;
import edu.tamu.tcat.trc.repo.VersionedRecord;

public class RecordReferenceImpl<RecordType> implements RecordReference<RecordType>
{
   private String id;
   private RecordType record;
   private Date created;
   private Date lastModified;
   private Boolean isRemoved;
   private Javers jvs;
   private List<CdoSnapshot> snapshots;
   
   RecordReferenceImpl()
   {
      jvs = JaversBuilder.javers().build();
      snapshots = jvs.findSnapshots(QueryBuilder.byInstanceId(id, record.getClass()).build());
   }
   
   void setId(String id)
   {
      this.id = id;
   }
   
   void setRecordData(RecordType data)
   {
      this.record = data;
   }
   
   void setDateCreated(Date created)
   {
      this.created = created;
   }
   
   void setDateModified(Date modified)
   {
      this.lastModified = modified;
   }
   
   void setRemovedState(Date removed)
   {
      this.isRemoved = removed == null ? false : true;
   }

   @Override
   public String getId()
   {
      return this.id;
   }

   @Override
   public boolean isDeleted()
   {
      return isRemoved.booleanValue();
   }

   @Override
   public Account getOwner()
   {
      return null;
   }

   @Override
   public Instant getDateCreated()
   {
      return this.created.toInstant();
   }

   @Override
   public Instant getLastModified()
   {
      return this.lastModified.toInstant();
   }

   @Override
   public List<VersionMeta> listVersions(VersionFilter filter)
   {
      return snapshots.stream().map(snap -> getMeta(snap)).collect(Collectors.toList());
   }
   
   private VersionMeta getMeta(CdoSnapshot snapshot)
   {
      return new VersionMetaImpl(snapshot);
   }

   @Override
   public VersionedRecord<RecordType> getVersion(String versionId)
   {
      return new VersionedRecordImpl(snapshots.stream().filter(snap -> snap.getVersion() == Long.parseLong(versionId)).findFirst().orElse(null));
   }

   @Override
   public RecordType get()
   {
      return record;
   }
   
   private class VersionMetaImpl implements VersionMeta
   {
      private final CdoSnapshot snapshot;

      VersionMetaImpl (CdoSnapshot snapshot)
      {
         this.snapshot = snapshot;
      }
      
      @Override
      public String getVersionId()
      {
         return Long.toString(snapshot.getVersion());
      }

      @Override
      public String getRecordId()
      {
         return snapshot.getGlobalId().value();
      }

      @Override
      public Instant getDate()
      {
         return snapshot.getCommitMetadata().getCommitDate().toDate().toInstant();
      }

      @Override
      public Account getActor()
      {
         String author = snapshot.getCommitMetadata().getAuthor();
         return null;
      }
   }
   
   private class VersionedRecordImpl implements VersionedRecord<RecordType>
   {
      
      private CdoSnapshot snapshot;
      private CommitMetadata metaData;

      VersionedRecordImpl(CdoSnapshot snapshot)
      {
         this.snapshot = snapshot;
         metaData = snapshot.getCommitMetadata();
      }

      @Override
      public String getRecordId()
      {
         return snapshot.getGlobalId().value();
      }

      @Override
      public String getVersionId()
      {
         return Long.toString(snapshot.getVersion());
      }

      @Override
      public Instant getModificationDate()
      {
         return metaData.getCommitDate().toDate().toInstant();
      }

      @Override
      public Account getActor()
      {
         String author = metaData.getAuthor();
         return null;
      }

      @Override
      public RecordType getRecord()
      {
         // We'll need to create an adapter to make this work.
         return null;
      }
      
   }
}
