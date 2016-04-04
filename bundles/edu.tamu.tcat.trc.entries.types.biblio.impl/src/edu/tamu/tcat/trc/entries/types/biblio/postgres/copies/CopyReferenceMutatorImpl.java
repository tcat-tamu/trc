package edu.tamu.tcat.trc.entries.types.biblio.postgres.copies;

import java.util.Map;

import edu.tamu.tcat.trc.entries.types.biblio.dto.copies.CopyReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceMutator;

public class CopyReferenceMutatorImpl implements CopyReferenceMutator
{
   private CopyReferenceDTO copyReference;

   public CopyReferenceMutatorImpl(CopyReferenceDTO dto)
   {
      this.copyReference = dto;
   }

   @Override
   public String getId()
   {
      return copyReference.id;
   }

   @Override
   public void setType(String type)
   {
      copyReference.type = type;
   }

   @Override
   public void setProperties(Map<String, String> properties)
   {
      copyReference.properties = properties;
   }

   @Override
   public void setTitle(String title)
   {
      copyReference.title = title;
   }

   @Override
   public void setSummary(String summary)
   {
      copyReference.summary = summary;
   }

   @Override
   public void setRights(String description)
   {
      copyReference.rights = description;
   }
}
