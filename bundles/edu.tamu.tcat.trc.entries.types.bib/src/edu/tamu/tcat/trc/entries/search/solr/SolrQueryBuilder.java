package edu.tamu.tcat.trc.entries.search.solr;

import java.util.Collection;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.params.SolrParams;

import edu.tamu.tcat.trc.entries.search.SearchException;

/**
 * A query builder for a SOLR query. This API is intended to wrap the basic REST API
 * of SOLR and in turn be wrapped per search core with an API which provides
 * application-specific, semantic details.
 */
public interface SolrQueryBuilder
{
   /**
    * Get the result of this builder as a {@link SolrParams}
    * to be executed using {@link SolrServer#query(SolrParams)}
    */
   SolrParams get() throws SearchException;

   void offset(int offset);

   void max(int max);

   /**
    * Set the "basic" search terms.
    */
   void basic(String q) throws SearchException;

   /**
    * Add query criteria where the parameter "matches" the provided value.
    * This is used for advanced query parameters.
    */
   <P> void query(Parameter<P> param, P value) throws SearchException;

   /**
    * Add filter criteria where the parameter equals the provided value. This
    * is used for facets.
    */
   <P> void filter(Parameter<P> param, Collection<P> values) throws SearchException;

   interface Parameter<T>
   {
      String getName();
      Class<T> getType();

      String toSolrValue(T value) throws SearchException;
   }
}
