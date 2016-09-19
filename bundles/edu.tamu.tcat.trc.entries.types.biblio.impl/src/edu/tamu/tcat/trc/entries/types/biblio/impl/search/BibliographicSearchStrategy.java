package edu.tamu.tcat.trc.entries.types.biblio.impl.search;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.IndexServiceStrategy;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;

public class BibliographicSearchStrategy implements IndexServiceStrategy<BibliographicEntry, WorkSolrQueryCommand>
{
   public static final String SOLR_CORE = "bibliographical";

   public BibliographicSearchStrategy()
   {
   }

   @Override
   public Class<BibliographicEntry> getType()
   {
      return BibliographicEntry.class;
   }

   @Override
   public String getCoreId()
   {
      return SOLR_CORE;
   }

   @Override
   public SolrIndexConfig getIndexCofig()
   {
      return new BiblioSolrConfig();
   }

   @Override
   public SolrInputDocument getDocument(BibliographicEntry entry)
   {
      return IndexAdapter.createWork(entry);
   }

   @Override
   public String getEntryId(BibliographicEntry entry)
   {
      return entry.getId();
   }

   @Override
   public WorkSolrQueryCommand createQuery(SolrClient solr)
   {
      try
      {
         TrcQueryBuilder builder = new TrcQueryBuilder(getIndexCofig());
         return new WorkSolrQueryCommand(solr, builder);
      }
      catch (SearchException ex)
      {
         throw new IllegalStateException("Failed to construct query builder", ex);
      }
   }
}
