package edu.tamu.tcat.trc.entries.types.bio.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

   private static BundleContext context;

   public static BundleContext getContext() {
      return context;
   }

   @Override
   public void start(BundleContext context) throws Exception {
      Activator.context = context;
   }

   @Override
   public void stop(BundleContext context) throws Exception {
      Activator.context = null;
   }

}
