package edu.tamu.tcat.trc.entries.types.reln;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import edu.tamu.tcat.trc.resolver.EntryId;

public abstract class RelationshipFilters
{
   public static Predicate<Relationship> createTypeFilter(RelationshipType type)
   {
      return (reln) -> Objects.equals(reln.getType().getIdentifier(), type.getIdentifier());
   }

   public static Predicate<Relationship> createAnchorFilter(EntryId root, String relatedType)
   {
      return (reln) -> {
         RelationshipType type = reln.getType();

         return type.isDirected() && Relationship.contains(reln.getRelatedEntities(), root)
               ? checkTargetsType(reln.getTargetEntities(), relatedType)
               : checkTargetsType(reln.getRelatedEntities(), relatedType);
      };
   }

   private static boolean checkTargetsType(Collection<Anchor> anchors, String entryType)
   {
      return anchors.stream()
            .map(Anchor::getTarget)
            .allMatch(eId -> eId.getType().equals(entryType));
   }

   public static Predicate<Relationship> and(Predicate<Relationship>... predicates)
   {
      return (reln) -> Stream.of(predicates).allMatch(pred -> pred.test(reln));
   }

   public static Predicate<Relationship> or(Predicate<Relationship>... predicates)
   {
      return (reln) -> Stream.of(predicates).anyMatch(pred -> pred.test(reln));
   }
}
