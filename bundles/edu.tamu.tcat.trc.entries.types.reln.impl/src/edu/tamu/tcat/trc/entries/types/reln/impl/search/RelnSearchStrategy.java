package edu.tamu.tcat.trc.entries.types.reln.impl.search;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipQueryCommand;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.IndexServiceStrategy;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;

public class RelnSearchStrategy implements IndexServiceStrategy<Relationship, RelationshipQueryCommand>
{

   @Override
   public Class<Relationship> getType()
   {
      return Relationship.class;
   }

   @Override
   public String getCoreId()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public SolrIndexConfig getIndexCofig()
   {
      return new RelnSolrConfig();
   }

   @Override
   public SolrInputDocument getDocument(Relationship entry)
   {
      return RelnDocument.create(entry);
   }

   @Override
   public String getEntryId(Relationship entry)
   {
      return entry.getId();
   }

   @Override
   public RelationshipQueryCommand createQuery(SolrClient solr)
   {
      try
      {
         TrcQueryBuilder builder = new TrcQueryBuilder(getIndexCofig());
         return new RelationshipSolrQueryCommand(solr, builder);
      }
      catch (SearchException ex)
      {
         throw new IllegalStateException("Failed to construct query builder", ex);
      }
   }

}
