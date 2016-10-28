package edu.tamu.tcat.trc.entries.types.reln.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.DerivedRelationship;
import edu.tamu.tcat.trc.entries.types.reln.GroupedRelationshipSet;
import edu.tamu.tcat.trc.entries.types.reln.GroupedRelationshipSet.TypedRelationshipGroup;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

public class GroupedRelationshipSetBuilder
{
   private final AtomicBoolean isBuilt = new AtomicBoolean();

   private final EntryId entry;
   private final ConcurrentHashMap<String, TypedRelationshipGroup> groups = new ConcurrentHashMap<>();

   private final EntryResolverRegistry resolvers;

   public GroupedRelationshipSetBuilder(EntryId entry, EntryResolverRegistry resolvers)
   {
      this.entry = entry;
      this.resolvers = resolvers;
   }

   public void add(DerivedRelationship reln)
   {
      String typeId = reln.getType().getIdentifier();
      groups.computeIfAbsent(typeId, key -> new TypedRelnGroupImpl(reln.getType()));
   }

   public class GroupedRelnSetImpl implements GroupedRelationshipSet
   {
      @Override
      public EntryId getRootEntity()
      {
         return entry;
      }

      @Override
      public Collection<TypedRelationshipGroup> getRelationshipGroups()
      {
         return groups.values();
      }
   }

   // TODO merge relationships
   // TODO merge anchors
   // TODO test anchor set equality

   private class TypedRelnGroupImpl implements TypedRelationshipGroup
   {
      private RelationshipType type;

      TypedRelnGroupImpl(RelationshipType type)
      {
         this.type = type;
         HashSet<String> a = new HashSet<>();
         HashSet<String> b = new HashSet<>();
      }

      void add(DerivedRelationship reln)
      {
         Relationship.contains(reln.getRelatedEntities(), entry);
         TreeSet<Anchor> from = new TreeSet<>(this::compare);
         from.addAll(reln.getRelatedEntities());
         TreeSet<Anchor> to = new TreeSet<>(this::compare);
         to.addAll(reln.getTargetEntities());
      }

      int compare(Anchor a, Anchor b)
      {
         String tokenA = resolvers.tokenize(a.getTarget());
         String tokenB = resolvers.tokenize(b.getTarget());

         return tokenA.compareTo(tokenB);
      }

      @Override
      public EntryId getRootEntity()
      {
         return entry;
      }

      @Override
      public RelationshipType getType()
      {
         return type;
      }

      @Override
      public Set<DerivedRelationship> getRelationshipsFrom()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public Set<DerivedRelationship> getRelationshipsTo()
      {
         // TODO Auto-generated method stub
         return null;
      }
   }
}
