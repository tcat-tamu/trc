package edu.tamu.tcat.trc.search.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;

/**
 * Defines a strategy for adapting domain objects into documents that can be indexed by Solr
 * and for generating domain specific query commands that can be executed against the
 * underlying search index. In general, a strategy instance maps a particular domain object
 * onto the Solr core to be used to index instances of that type and provides
 *
 * @param <Entry>
 * @param <QueryCmd>
 */
public interface IndexServiceStrategy<Entry, QueryCmd>
{
   /**
    * @return The Java type of the input object this strategy supports. There can be only
    *    one {@link IndexServiceStrategy} in use for any given Java type.
    */
   Class<Entry> getType();

   /**
    * Returns a logical identifier for the Solr core to be used to index data associated
    * with this type of object. This identifier will be used to lookup specific properties for
    * this core from the application's {@link ConfigurationProperties}.
    *
    * @return The logical identifier for the Solr core.
    */
   String getCoreId();

   /**
    * @return The index configuration used to describe the various fields associated
    *       with the {@link SolrInputDocument} that will be produced by
    *       {@link #getDocument(Object)}.
    */
   SolrIndexConfig getIndexCofig();

   SolrInputDocument getDocument(Entry entry);

   String getEntryId(Entry entry);

   QueryCmd createQuery(SolrClient solr);
}

