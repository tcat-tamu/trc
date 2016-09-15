package edu.tamu.tcat.trc.search.solr;

/**
 * Designed to be a core framework service used to construct and manage {@link IndexService} instances
 *
 */
public interface SearchServiceManager
{
   boolean isEnabled();

   // TODO need better way to identify just what we need on query

   <Entry, QueryCmd> IndexService<Entry> configure(IndexServiceStrategy<Entry, QueryCmd> indexCfg);

   <Entry> IndexService<Entry> getIndexService(Class<Entry> type);

   <Entry, QueryCmd> QueryService<QueryCmd> getQueryService(IndexServiceStrategy<Entry, QueryCmd> indexCfg);
}
