package edu.tamu.tcat.trc.search.solr;

/**
 * Designed to be a core framework service used to construct and manage {@link IndexService} instances
 *
 */
public interface IndexServiceFactory
{

   boolean isEnabled();

   <Entry, QueryCmd> IndexService<Entry, QueryCmd> getIndexService(IndexServiceStrategy<Entry, QueryCmd> indexCfg);
}
