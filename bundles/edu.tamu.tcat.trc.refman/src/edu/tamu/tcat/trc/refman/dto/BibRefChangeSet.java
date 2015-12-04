package edu.tamu.tcat.trc.refman.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BibRefChangeSet
{
   public final String id;

   public String type = null;
   public Map<String, String> values = new HashMap<>();
   public List<CreatorDTO> creators;

   public BibRefDTO original;

   public BibRefChangeSet(String id)
   {
      this.id = id;
   }
}