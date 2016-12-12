package edu.tamu.tcat.trc.repo.postgres;

import static java.text.MessageFormat.format;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.javers.core.Javers;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.joda.time.LocalDateTime;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.repo.RecordReference;
import edu.tamu.tcat.trc.repo.VersionFilter;
import edu.tamu.tcat.trc.repo.VersionMeta;
import edu.tamu.tcat.trc.repo.VersionedRecord;

public class RecordReferenceImpl<RecordType> implements RecordReference<RecordType>
{
   private final Map<String, RecordType> cache = new ConcurrentHashMap<>();
   private final Javers jvs;
   private final Supplier<RecordType> recordSupplier;

   private String id;
   private Date created;
   private Date lastModified;
   private Boolean isRemoved;


   RecordReferenceImpl(Javers jvs, Supplier<RecordType> recordSupplier)
   {
      this.recordSupplier = recordSupplier;
      this.jvs = jvs;
   }

   void setId(String id)
   {
      this.id = id;
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
   public RecordType get()
   {
      return cache.computeIfAbsent(id, id -> recordSupplier.get());
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
      // TODO implement this
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
      List<CdoSnapshot> snapshots = jvs.findSnapshots(buildQuery(filter));
      return snapshots.stream()
            .map(VersionMetaImpl::new)
            .collect(Collectors.toList());
   }

   private static LocalDateTime toJoda(java.time.LocalDateTime time)
   {
      return new LocalDateTime(time.getYear(), time.getMonthValue(), time.getDayOfMonth(),
                        time.getHour(), time.getMinute(), time.getSecond());
   }

   private JqlQuery buildQuery(VersionFilter filter)
   {
      // TODO finish applying version filter
      QueryBuilder qBuilder = QueryBuilder.byInstanceId(id, get().getClass());
      filter.before().map(RecordReferenceImpl::toJoda).ifPresent(qBuilder::to);
      filter.after().map(RecordReferenceImpl::toJoda).ifPresent(qBuilder::from);
      filter.limit().ifPresent(qBuilder::limit);

      return qBuilder.build();
   }

   @Override
   public VersionedRecord<RecordType> getVersion(String versionId)
   {
      QueryBuilder qBuilder = QueryBuilder.byInstanceId(id, get().getClass());
      qBuilder.withVersion(Long.parseLong(versionId));
      List<CdoSnapshot> snapshots = jvs.findSnapshots(qBuilder.build());
      if (snapshots.isEmpty())
         throw new IllegalArgumentException(format("No version of [(0}] exists with version id {1}.", getId(), versionId));

      return new VersionedRecordImpl(snapshots.get(0));
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
         // TODO implement method
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
         // TODO implement method
         String author = metaData.getAuthor();
         return null;
      }

      @Override
      public RecordType getRecord()
      {
         // TODO implement method
         // We'll need to create an adapter to make this work.
         return null;
      }

   }
}
