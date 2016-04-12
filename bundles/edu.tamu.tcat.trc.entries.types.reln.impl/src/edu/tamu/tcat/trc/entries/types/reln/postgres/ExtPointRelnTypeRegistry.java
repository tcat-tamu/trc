/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.entries.types.reln.postgres;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.RegistryFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipException;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;

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
   public static final String EXT_POINT_ID = "edu.tamu.tcat.trc.entries.types.reln.reltypes";

   private final AtomicReference<AutoCloseable> regEarsRef = new AtomicReference<>();
   private final ConcurrentMap<String, ExtRelationshipTypeDefinition> typeDefinitions = new ConcurrentHashMap<>();
   private final CountDownLatch initComplete = new CountDownLatch(1);

   public ExtPointRelnTypeRegistry()
   {
   }

   /**
    * Initializes this {@link ExtPointRelnTypeRegistry}
    */
   public void activate()
   {
      loadExtensions();
   }

   public void dispose()
   {
      // unregister listener
      AutoCloseable regEars = regEarsRef.get();
      if (regEars != null)
      {
         try {
            regEars.close();
         } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed unregistering", e);
         }
         regEarsRef.set(null);
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
      ExecutorService exec = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("reln ext loader").build());
      exec.submit(() -> {
         try
         {
            IExtensionRegistry registry = RegistryFactory.getRegistry();
            RegistryEventListener ears = new RegistryEventListener();
            // Since we have the registry and ears now, encapsulate them in an AutoCloseable to make unregistering
            // more simple. Store in an AtomicReference to ensure thread-safety from this executor thread to
            // whatever disposes the service.
            regEarsRef.set(() -> {
               registry.removeListener(ears);
            });
            registry.addListener(ears, EXT_POINT_ID);
      
            // register any currently loaded transformers
            IExtension[] extensions = registry.getExtensionPoint(EXT_POINT_ID).getExtensions();
            for (IExtension ext : extensions)
            {
               ears.parseExtension(ext);
            }
            
            initComplete.countDown();
         }
         catch (Exception e)
         {
            // consume the exception and log instead of throwing since we are not holding the Future
            // but using the latch.
            logger.log(Level.SEVERE, "Failed initializing registry", e);
         }
         finally
         {
            // terminate the executor's threads since the single task is now complete
            exec.shutdown();
         }
      });
   }

   @Override
   public RelationshipType resolve(String typeIdentifier) throws RelationshipException
   {
      try
      {
         // wait a respectable time, but not forever
         initComplete.await(10, TimeUnit.SECONDS);
      }
      catch (Exception e)
      {
         throw new RelationshipException("Relationship types failed initializing", e);
      }
      
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
