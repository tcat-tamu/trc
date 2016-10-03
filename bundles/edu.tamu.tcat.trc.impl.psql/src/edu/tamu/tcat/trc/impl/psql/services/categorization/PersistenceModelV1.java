package edu.tamu.tcat.trc.impl.psql.services.categorization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;

public abstract class PersistenceModelV1
{

   public static class CategorizationScheme
   {
      public String id;
      public String scopeId;
      public String key;
      public String strategy;
      public String title;
      public String description;
   }

   public static class ListCategorizationStrategy extends CategorizationScheme
   {
      public List<CategorizationNode> nodes = new ArrayList<>();
   }

   public static class SetCategorizationStrategy extends CategorizationScheme
   {
      public Set<CategorizationNode> nodes = new HashSet<>();
   }

   public static class TreeCategorizationStrategy extends CategorizationScheme
   {
      public String root;
      public Map<String, TreeNode> nodes = new HashMap<>();
   }

   public static class CategorizationNode
   {
      public String id;
      public String label = "";
      public String description = "";

      public EntryReference ref;

      @Override
      public String toString()
      {
         return "Node " + label;
      }
   }

   public static class TreeNode extends CategorizationNode
   {
      public String parentId;
      public List<String> children = new ArrayList<>();
   }
}
