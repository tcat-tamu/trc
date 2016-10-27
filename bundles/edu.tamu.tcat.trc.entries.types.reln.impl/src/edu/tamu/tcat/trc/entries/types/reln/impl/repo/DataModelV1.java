package edu.tamu.tcat.trc.entries.types.reln.impl.repo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DataModelV1
{

   public static class Relationship
   {
      /** The unique identifier for this relationship. */
      public String id;

      /** The string-based type identifier for this relationship. */
      public String typeId;

      /** An editorial description of this relationship. */
      public String description;

      /** The set of anchors associated with the source endpoint of this relationship. */
      public List<Anchor> related = new ArrayList<>();

      /** The set of anchors that are referenced by this relationship. This will be
       *  non-empty only if the associated relationship is directed.
       */
      public List<Anchor> targets = new ArrayList<>();
   }

   public static class Anchor
   {
      /** A tokenized representation of the entry reference associated with this anchor. */
      public String ref;

      /** Application defined properties associated with this anchor. */
      public Map<String, String> properties = new HashMap<>();
   }

   public static class RelationshipType
   {
      /** A unique, semi-readable identifier for this type of relationship. */
      public String id;

      /** A display title for this relationship. For directed relationships, this
       *  corresponds to reading from the related entries to the target entries. */
      public String title;

      /** A display title for this relationship when reading from the reverse direction,
       *  that is, when reading from the target to the related entries. Only applicable for
       *  directed relationships. */
      public String reverse;

      /** A description of the nature and purpose of this type of relationship. */
      public String description;

      /** Indicates whether the relationship is directed. */
      public boolean isDirected;
   }

}
