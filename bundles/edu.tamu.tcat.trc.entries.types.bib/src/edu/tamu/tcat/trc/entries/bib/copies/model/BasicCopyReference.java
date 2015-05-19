package edu.tamu.tcat.trc.entries.bib.copies.model;

import java.net.URI;
import java.util.UUID;

import edu.tamu.tcat.trc.entries.bib.copies.CopyReference;

public class BasicCopyReference implements CopyReference
{
   private final UUID id;
   private final URI associatedEntry;
   private final String copyId;
   private final String title;
   private final String summary;
   private final String rights;

   public BasicCopyReference(UUID id, URI entry, String copyId, String title, String summary, String rights)
   {
      this.id = id;
      this.associatedEntry = entry;
      this.copyId = copyId;

      this.title = title != null ? title : "";
      this.summary = summary != null ? summary : "";
      this.rights = rights != null ? rights : "";
   }

   @Override
   public UUID getId()
   {
      return id;
   }
   @Override
   public URI getAssociatedEntry()
   {
      return associatedEntry;
   }
   @Override
   public String getCopyId()
   {
      return copyId;
   }
   @Override
   public String getTitle()
   {
      return title;
   }
   @Override
   public String getSummary()
   {
      return summary;
   }
   @Override
   public String getRights()
   {
      return rights;
   }
}
