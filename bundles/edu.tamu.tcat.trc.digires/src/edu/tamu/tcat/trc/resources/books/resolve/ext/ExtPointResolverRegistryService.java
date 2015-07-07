package edu.tamu.tcat.trc.resources.books.resolve.ext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.transform.Transformer;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.Platform;

import edu.tamu.tcat.trc.resources.books.resolve.CopyResolverRegistry;
import edu.tamu.tcat.trc.resources.books.resolve.CopyResolverStrategy;
import edu.tamu.tcat.trc.resources.books.resolve.DigitalCopy;
import edu.tamu.tcat.trc.resources.books.resolve.ResourceAccessException;
import edu.tamu.tcat.trc.resources.books.resolve.UnsupportedCopyTypeException;

/**
 * An extension point based {@link CopyResolverRegistry} intended to be registered via
 * declarative services.
 *
 */
public class ExtPointResolverRegistryService implements CopyResolverRegistry
{
   public static final String EXT_POINT_ID = "edu.tamu.tcat.trc.entries.bib.copy.resolver";

   private RegistryEventListener ears;

   private final ConcurrentMap<String, ExtTransformerFactoryDefinition> resolvers = new ConcurrentHashMap<>();


   /**
    * Initializes this {@link ExtPointTranformerFactoryProvider}. This must be called during
    * initial configuration in order to load {@link Transformer} plugins.
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
      resolvers.clear();
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
   public <T extends DigitalCopy> T resolve(String identifier, Class<T> copyType) throws ResourceAccessException, UnsupportedCopyTypeException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public <T extends DigitalCopy> CopyResolverStrategy<T> getResolver(Class<T> resolverType)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public CopyResolverStrategy<? extends DigitalCopy> getResolver(String identifier)
   {
      // TODO Auto-generated method stub
      return null;
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
         // TODO Auto-generated method stub

      }

      @Override
      public void removed(IExtension[] extensions)
      {
         // TODO Auto-generated method stub

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
