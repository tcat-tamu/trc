package edu.tamu.tcat.trc.entries.types.reln.impl.model;

import static java.text.MessageFormat.format;

import java.util.HashMap;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.impl.repo.DataModelV1;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

public class AnchorImpl implements Anchor
{
   private final HashMap<String, Set<String>> properties;
   private final EntryId ref;

   public AnchorImpl(DataModelV1.Anchor dto, EntryResolverRegistry resolvers)
   {
      this.properties = new HashMap<>(dto.properties);
      this.ref = resolvers.decodeToken(dto.ref);
   }

   @Override
   public EntryId getTarget()
   {
      return ref;
   }

   @Override
   public Set<String> listProperties()
   {
      return properties.keySet();
   }

   @Override
   public Set<String> getProperty(String property)
   {
      if (!properties.containsKey(property))
         throw new IllegalArgumentException(format("Undefined property {0}", property));

      return properties.get(property);
   }
}
