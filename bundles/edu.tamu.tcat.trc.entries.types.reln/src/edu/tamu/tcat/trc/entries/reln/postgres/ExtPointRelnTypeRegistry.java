package edu.tamu.tcat.trc.entries.reln.postgres;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.Platform;

import edu.tamu.tcat.trc.entries.reln.RelationshipException;
import edu.tamu.tcat.trc.entries.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.reln.RelationshipTypeRegistry;

/**
 * Supports Eclipse Extension Point type registrations. Intended to be registered as an OSGi service
 * by the application.
 *
 * <p>
 * Note that in general, we are likely to need to provide end users with the ability to extend the
 * relationship type registration. This would require a DB backed impl rather than extension points.
 * Ext Points become valueable as a means to provide application-defined metadata based extension of
 * relationship types.
 */
public class ExtPointRelnTypeRegistry implements RelationshipTypeRegistry
{
   private static final Logger logger = Logger.getLogger(ExtPointRelnTypeRegistry.class.getName());
   public static final String EXT_POINT_ID = "edu.tamu.tcat.catalogentries.relationships.types";

   private RegistryEventListener ears;
   private final ConcurrentMap<String, ExtRelationshipTypeDefinition> typeDefinitions = new ConcurrentHashMap<>();


   public ExtPointRelnTypeRegistry()
   {
   }

   /**
    * Initializes this {@link ExtPointRelnTypeRegistry}. .
    */
   public void activate()
   {
      // TODO do async and wait until ready.
      loadExtensions();
   }

   public void dispose()
   {
      // unregister listener
      if (ears != null)
      {
         IExtensionRegistry registry = Platform.getExtensionRegistry();
         registry.removeListener(ears);
      }

      // clear all loaded factory definitions
      typeDefinitions.clear();
   }

   /**
    * Loads all currently registered extensions of the the DataTrax Transformers extension
    * point and attaches a listener to the {@link IExtensionRegistry} that will be notified
    * when new extensions become available or loaded extensions are removed.
    *
    * <p>
    * This method is typically called by {@link #activate()} when the {@code ExtPointTranformerFactoryProvider}
    * is registered as an OSGi service or when the provider is manually activated.
    */
   private void loadExtensions()
   {
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      ears = new RegistryEventListener();
      registry.addListener(ears, EXT_POINT_ID);

      // register any currently loaded transformers
      IExtension[] extensions = registry.getExtensionPoint(EXT_POINT_ID).getExtensions();
      for (IExtension ext : extensions)
      {
         ears.parseExtension(ext);
      }
   }

   @Override
   public RelationshipType resolve(String typeIdentifier) throws RelationshipException
   {
      if (!typeDefinitions.containsKey(typeIdentifier))
         throw new RelationshipException("No relationship type is registered for '" + typeIdentifier + "'");

      return typeDefinitions.get(typeIdentifier);
   }

   @Override
   public Set<String> list()
   {
      return new HashSet<>(typeDefinitions.keySet());
   }

   private class RegistryEventListener implements IRegistryEventListener
   {

      @Override
      public void added(IExtension[] extensions)
      {
         for (IExtension ext : extensions)
         {
            parseExtension(ext);
         }
      }

      private void parseExtension(IExtension ext)
      {
         IConfigurationElement[] elements = ext.getConfigurationElements();

         for (IConfigurationElement e : elements)
         {
            ExtRelationshipTypeDefinition configuration = new ExtRelationshipTypeDefinition(e);
            String id = configuration.getIdentifier();
            if (typeDefinitions.containsKey(id))
            {
               ExtRelationshipTypeDefinition existing = typeDefinitions.get(id);
               logger.log(Level.WARNING, "Duplicate relationship type registration for [" + id + "]\n"
                     + "New:      " + configuration + "\n"
                     + "Existing: " + existing);
               continue;
            }
            typeDefinitions.putIfAbsent(id, configuration);

            // TODO log duplicate registration
         }
      }

      @Override
      public void removed(IExtension[] extensions)
      {
         for (IExtension ext : extensions)
         {
            IConfigurationElement[] elements = ext.getConfigurationElements();

            for (IConfigurationElement e : elements)
            {
               String id = e.getAttribute("id");
               typeDefinitions.remove(id);
            }
         }
      }

      @Override
      public void added(IExtensionPoint[] extensionPoints)
      {
         // no-op
      }

      @Override
      public void removed(IExtensionPoint[] extensionPoints)
      {
         // no-op
      }
   }
}
