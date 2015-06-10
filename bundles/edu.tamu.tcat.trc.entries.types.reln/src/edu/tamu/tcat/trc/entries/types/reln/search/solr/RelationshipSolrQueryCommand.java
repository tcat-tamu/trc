package edu.tamu.tcat.trc.entries.types.reln.search.solr;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import com.google.common.base.Joiner;

import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.search.solr.impl.TrcQueryBuilder;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipDirection;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipQueryCommand;
import edu.tamu.tcat.trc.entries.types.reln.search.RelnSearchProxy;

public class RelationshipSolrQueryCommand implements RelationshipQueryCommand
{
   private final static Logger logger = Logger.getLogger(RelationshipSolrQueryCommand.class.getName());
   private static final int DEFAULT_MAX_RESULTS = 25;

   private final SolrServer solr;
   private final TrcQueryBuilder qb;

   private Collection<String> criteria = new ArrayList<>();

   private RelationshipTypeRegistry typeReg;

   public RelationshipSolrQueryCommand(SolrServer solr, RelationshipTypeRegistry typeReg, TrcQueryBuilder qb)
   {
      this.solr = solr;
      this.typeReg = typeReg;
      this.qb = qb;
      qb.max(DEFAULT_MAX_RESULTS);
   }

   @Override
   public SolrRelnResults execute() throws SearchException
   {
      try
      {
         //HACK: relationship query should use edismax
         String queryString = Joiner.on(" AND ").join(criteria);
         qb.basic(queryString);
         QueryResponse response = solr.query(qb.get());
         SolrDocumentList results = response.getResults();

         List<RelnSearchProxy> relns = qb.unpack(results, RelnSolrConfig.SEARCH_PROXY);
         return new SolrRelnResults(this, relns);
      }
      catch (Exception e)
      {
         throw new SearchException("An error occurred while querying the author core: " + e, e);
      }
   }

   @Override
   public void forEntity(URI entity, RelationshipDirection direction)
   {
      Objects.requireNonNull(entity, "Entity URI must be provided");
      String entityString = entity.toString();

      switch(direction)
      {
         case any:
            criteria.add("(relatedEntities:\"" + entityString + "\" OR targetEntities:\"" + entityString + "\")");
            break;
         case to:
            criteria.add("targetEntities:\"" + entityString + "\"");
            break;
         case from:
            criteria.add("relatedEntities:\"" + entityString + "\"");
            break;
         default:
            throw new IllegalStateException("Relationship direction not defined");
      }
   }

   @Override
   public void byType(String typeId)
   {
      Objects.requireNonNull(typeId, "typeId may not be null");
      criteria.add("relationshipType:\"" + typeId + "\"");
   }

   @Override
   public void setOffset(int start)
   {
      if (start < 0)
         throw new IllegalArgumentException("Offset ["+start+"] cannot be negative");

      qb.offset(start);
   }

   @Override
   public void setMaxResults(int max)
   {
      qb.max(max);
   }
}
