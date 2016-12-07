package edu.tamu.tcat.trc.repo.postgres;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
      return Collections.emptyList();
   }

   @Override
   public VersionedRecord<RecordType> getVersion(String versionId)
   {
      return null;
   }

   @Override
   public RecordType get()
   {
      return record;
   }
}
