package edu.tamu.tcat.trc.search.solr;

/**
 * Designed to be a core framework service used to construct and manage {@link IndexService} instances
 *
 */
public interface IndexServiceFactory
{
   boolean isEnabled();

   // TODO separate registration from retrieval

   <Entry, QueryCmd> IndexService<Entry> getIndexService(IndexServiceStrategy<Entry, QueryCmd> indexCfg);

   <Entry, QueryCmd> QueryService<QueryCmd> getQueryService(IndexServiceStrategy<Entry, QueryCmd> indexCfg);
}
