package edu.tamu.tcat.sda.datastore;

/**
 * Provides a central point of access for retrieving the data stores that have been 
 * configured to work with the application
 *
 */
public interface DataStoreProvider
{
   /**
    * @param type the type of data store to return. 
    * @return a data store of the supplied type.
    * @throws DataStoreException If no data store of the requested type is available
    */
   <X extends DataStore> X get(Class<X> type) throws DataStoreException;
}
