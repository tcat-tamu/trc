package edu.tamu.tcat.trc.entries.types.reln.impl.search;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipQueryCommand;
import edu.tamu.tcat.trc.search.solr.IndexServiceStrategy;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;

public class RelnSearchStrategy implements IndexServiceStrategy<Relationship, RelationshipQueryCommand>
{

   public static final String SOLR_CORE = "relationships";

   private final TrcApplication trcCtx;

   public RelnSearchStrategy(TrcApplication trcCtx)
   {
      this.trcCtx = trcCtx;
   }

   @Override
   public Class<Relationship> getType()
   {
      return Relationship.class;
   }

   @Override
   public String getCoreId()
   {
      return SOLR_CORE;
   }

   @Override
   public SolrIndexConfig getIndexCofig()
   {
      return new RelnSolrConfig();
   }

   @Override
   public SolrInputDocument getDocument(Relationship entry)
   {
      return RelnDocument.create(entry, trcCtx.getResolverRegistry());
   }

   @Override
   public String getEntryId(Relationship entry)
   {
      return entry.getId();
   }

   @Override
   public RelationshipQueryCommand createQuery(SolrClient solr)
   {
      TrcQueryBuilder builder = new TrcQueryBuilder(getIndexCofig());
      return new RelationshipSolrQueryCommand(trcCtx, solr, builder);
   }

}
