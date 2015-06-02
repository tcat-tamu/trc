package edu.tamu.tcat.trc.entries.search.solr;

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
    * Each invocation will add additional criteria to any already stored in this builder
    * using an "OR" operation to combine any existing criteria to this new entry.
    * <p>
    * This is used for advanced query parameters.
    */
   <P> void query(SolrIndexField<P> param, P value) throws SearchException;

   /**
    * Add query criteria where the parameter falls within the provided range. The values
    * given for the range are inclusive.
    *
    * @param param
    * @param start
    * @param end
    * @throws SearchException
    */
   default <P> void queryRange(SolrIndexField<P> param, P start, P end) throws SearchException
   {
      queryRangeExclusive(param, start, end, false, false);
   }

   /**
    * Add query criteria where the parameter falls within the provided range with optional range
    * endpoint exclusivity. A call to {@code queryRangeExclusive(p, start, end, false, false)} is
    * equivalent to a call to {@code queryRange(p, start, end)}
    *
    * @param param
    * @param start
    * @param end
    * @param excludeStart
    * @param excludeEnd
    * @throws SearchException
    */
   <P> void queryRangeExclusive(SolrIndexField<P> param, P start, P end, boolean excludeStart, boolean excludeEnd) throws SearchException;

   /**
    * Add filter criteria where the parameter falls within the provided range. This
    * is used for facets.
    */
   <P> void filter(SolrIndexField<P> param, P value) throws SearchException;

   /**
    * Add filter criteria where the parameter falls within the provided range. This
    * is used for facets.
    */
   default <P> void filterRange(SolrIndexField<P> param, P start, P end) throws SearchException
   {
      filterRangeExclusive(param, start, end, false, false);
   }

   /**
    * Add filter criteria where the parameter equals the provided value. This
    * is used for facets.
    */
   <P> void filterRangeExclusive(SolrIndexField<P> param, P start, P end, boolean excludeStart, boolean excludeEnd) throws SearchException;
}
