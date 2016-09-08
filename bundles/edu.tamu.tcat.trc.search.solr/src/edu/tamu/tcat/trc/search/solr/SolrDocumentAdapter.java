package edu.tamu.tcat.trc.search.solr;

import org.apache.solr.common.SolrInputDocument;

/**
 * Adapts an input object of some type into a {@link SolrInputDocument}.
 *
 * @param <T> The type of input object this adapter supports.
 */
public interface SolrDocumentAdapter<T>
{

   /**
    * @param instance The Java object to be converted to a searchable representation.
    * @return The document to be indexed.
    */
   SolrInputDocument adapt(T instance);

}
