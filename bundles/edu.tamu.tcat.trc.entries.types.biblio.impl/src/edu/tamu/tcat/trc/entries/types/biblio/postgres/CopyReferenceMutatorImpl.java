package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import java.util.HashMap;
import java.util.Map;

import edu.tamu.tcat.trc.entries.types.biblio.dto.CopyReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.CopyReferenceMutator;
import edu.tamu.tcat.trc.repo.ChangeSet;

public class CopyReferenceMutatorImpl implements CopyReferenceMutator
{
//   private CopyReferenceDTO copyReference;
   private final String id;
   private final ChangeSet<CopyReferenceDTO> changes;

   public CopyReferenceMutatorImpl(String id, ChangeSet<CopyReferenceDTO> refChanges)
   {
      this.id = id;
      this.changes = refChanges;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public void setType(String type)
   {
      changes.add("type", dto -> dto.type = type);
   }

   @Override
   public void setProperties(Map<String, String> properties)
   {
      Map<String, String> props = new HashMap<>(properties);
      changes.add("properties", dto -> dto.properties = props);
   }

   @Override
   public void setTitle(String title)
   {
      changes.add("title", dto -> dto.title = title);
   }

   @Override
   public void setSummary(String summary)
   {
      changes.add("summary", dto -> dto.summary = summary);
   }

   @Override
   public void setRights(String description)
   {
      changes.add("rights", dto -> dto.rights = description);
   }
}
