package edu.tamu.tcat.trc.entries.search.solr;

import org.apache.solr.client.solrj.SolrQuery;

import edu.tamu.tcat.trc.entries.search.SearchException;

/**
 * An application-specific configuration for a query builder implementation to use for
 * initialization, validation, and execution.
 */
public interface SolrQueryConfig
{
   void initialConfiguration(SolrQuery params) throws SearchException;
   void configureBasic(String q, SolrQuery params) throws SearchException;

   /**
    * The DTO type to use for a "search proxy", which is a serialized JSON data transfer
    * object stored in the index as a literal and retrieved as a single value to be the
    * representative for the object stored in the search index.
    */
   Class<?> getSearchProxyType();

   Class<?> getIndexDocumentType();

}
