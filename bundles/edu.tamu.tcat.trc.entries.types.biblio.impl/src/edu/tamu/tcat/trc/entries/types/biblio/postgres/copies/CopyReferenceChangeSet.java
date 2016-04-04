package edu.tamu.tcat.trc.entries.types.biblio.postgres.copies;

import java.util.Map;

import edu.tamu.tcat.trc.entries.types.biblio.dto.copies.CopyReferenceDTO;

public class CopyReferenceChangeSet
{
   public String id;
   public String type;
   public Map<String, String> properties;
   public String title;
   public String summary;
   public String rights;

   public CopyReferenceDTO original;

   public CopyReferenceChangeSet(String id)
   {
      this.id = id;
   }
}
