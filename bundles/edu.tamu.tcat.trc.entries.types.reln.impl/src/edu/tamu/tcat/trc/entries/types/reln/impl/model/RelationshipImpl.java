package edu.tamu.tcat.trc.entries.types.reln.impl.model;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.impl.repo.DataModelV1;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

public class RelationshipImpl implements Relationship
{
   private final String id;
   private final RelationshipType type;
   private final String description;
   private final List<Anchor> related;
   private final List<Anchor> targets;

   public RelationshipImpl(DataModelV1.Relationship dto,
                           RelationshipTypeRegistry typeReg,
                           EntryResolverRegistry resolvers)
   {
      this.id = dto.id;
      this.type = typeReg.resolve(dto.typeId);

      this.description = dto.description;
      this.related = dto.related.stream()
         .map(a -> new AnchorImpl(a, resolvers))
         .collect(toList());
      this.targets = dto.targets.stream()
            .map(a -> new AnchorImpl(a, resolvers))
            .collect(toList());
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public RelationshipType getType()
   {
      return type;
   }

   @Override
   public String getDescription()
   {
      return description;
   }

   @Override
   public Collection<Anchor> getRelatedEntities()
   {
      return Collections.unmodifiableList(related);
   }

   @Override
   public Collection<Anchor> getTargetEntities()
   {
      return Collections.unmodifiableList(targets);
   }
}
