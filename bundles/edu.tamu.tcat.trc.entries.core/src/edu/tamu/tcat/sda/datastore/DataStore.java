package edu.tamu.tcat.sda.datastore;

/**
 * Tag interface to be implemented by data store classes. 
 */
public interface DataStore
{
   // the goal here is to allow these to be registered as OSGi services and dynamically
   // loaded by an OSGi backed DataStoreService impl. Alternatively, they may be registered
   // as plugins
}
