package edu.tamu.tcat.trc.entries.types.reln.impl.model;

import static java.text.MessageFormat.format;

import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.impl.repo.DataModelV1;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

public class AnchorImpl implements Anchor
{
   private final static Logger logger = Logger.getLogger(AnchorImpl.class.getName());

   private final EntryResolverRegistry resolvers;

   private final EntryId entryId;
   private String label;
   private final HashMap<String, Set<String>> properties;

   public AnchorImpl(DataModelV1.Anchor dto, EntryResolverRegistry resolvers)
   {
      this.resolvers = resolvers;

      this.entryId = resolvers.decodeToken(dto.ref);
      this.label = dto.label;
      this.properties = dto.properties.isEmpty()? new HashMap<>() : new HashMap<>(dto.properties);
   }

   @Override
   public synchronized String getLabel()
   {
      if (label == null)
         label = loadLabel();

      return label;
   }

   private String loadLabel()
   {
      try
      {
         EntryReference<?> entryRef = resolvers.getReference(entryId);
         String result = entryRef.getLabel();
         return result != null ? result :  "No display label available";
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to retrieve label for anchor.", ex);
         return "Unknown Entry";
      }
   }

   @Override
   public EntryId getTarget()
   {
      return entryId;
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
