package edu.tamu.tcat.trc.search.solr;

import java.util.concurrent.TimeUnit;

import org.apache.solr.client.solrj.SolrClient;

/**
 * Defines an API for managing a SolrIndex for objects of a specific type. An
 * {@code IndexService} instance is intended to be associated with a single Solr core
 * and constructed using an {@link IndexServiceBuilder}. Note that multiple
 * {@code IndexService} instances may be built to handle different domain types that
 * are backed by the same underlying Solr core.
 *
 * <p>The {@code IndexService} is designed to support mapping arbitrarily complex
 * domain objects into 'documents' that have been carefully designed to support specific
 * search requirements.
 *
 * @param <T> The Java type of object to be stored in the associated index.
 */
public interface IndexService<T>
{
   /**
    * @return A Solr client configured to access the Solr core this index uses.
    *       Note that the returned client <em>must not</em> be closed by the caller.
    */
   SolrClient getSolrClient();

   /**
    * Indicates whether the supplied object has been indexed. Note that this
    * does not check to ensure that the current state of the indexed document
    * matches the state of the supplied object.
    *
    * @param instance the object to check.
    * @return <code>true</code> if this object is present in the index.
    */
   boolean isIndexed(T instance);

   /**
    * Adds the supplied object to the index or updates the current representation of this
    * document.
    *
    * @param instance The object to index.
    */
   void index(T instance);

   /**
    * Removes an object from the index.
    *
    * @param instance The object to be removed.
    */
   void remove(T instance);

   /**
    * Removes one or more object from the index by their ids.
    * @param ids The ids of the object or objects to be removed from the index.
    */
   void remove(String... ids);

   /**
    * Shuts down this index services and releases any bound resources.
    *
    * @param time The amount of time to wait for the service to shutdown
    * @param unit The time units associated with the amount of time to wait.
    */
   void shutdown(long time, TimeUnit unit);
}
