package edu.tamu.tcat.trc.search.solr;

public interface QueryService<QueryCmd>
{
   /**
    * @return A domain-specific query command that can be used to construct a query against
    *       the underlying Solr index.
    */
   QueryCmd createQuery();
}
