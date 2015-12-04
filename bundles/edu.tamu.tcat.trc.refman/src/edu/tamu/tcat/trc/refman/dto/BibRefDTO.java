package edu.tamu.tcat.trc.refman.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BibRefDTO
{
   public String collectionId;
   public String id;
   public String type;
   public Map<String, String> values = new HashMap<>();
   public List<CreatorDTO> creators = new ArrayList<>();
}
