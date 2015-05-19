package edu.tamu.tcat.trc.entries.reln.solr;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.google.common.base.Joiner;

import edu.tamu.tcat.trc.entries.reln.Relationship;
import edu.tamu.tcat.trc.entries.reln.RelationshipDirection;
import edu.tamu.tcat.trc.entries.reln.RelationshipException;
import edu.tamu.tcat.trc.entries.reln.RelationshipQueryCommand;
import edu.tamu.tcat.trc.entries.reln.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.entries.reln.model.RelationshipDV;

public class RelationshipSolrQueryCommand implements RelationshipQueryCommand
{
   private final static Logger logger = Logger.getLogger(RelationshipSolrQueryCommand.class.getName());

   private SolrQuery query = new SolrQuery();
   private Collection<String> criteria = new ArrayList<>();

   private RelationshipTypeRegistry typeReg;
   private SolrServer solr;


   public RelationshipSolrQueryCommand(SolrServer solr, RelationshipTypeRegistry typeReg)
   {
      this.solr = solr;
      this.typeReg = typeReg;

      // HACK: Return all, until we build in a paging system.
      query.setRows(Integer.valueOf(100));
   }

   @Override
   public Collection<Relationship> getResults()
   {
      Collection<Relationship> relationships = new HashSet<>();
      SolrDocumentList results = null;
      String relationshipJson = null;
      RelationshipDV dv = new RelationshipDV();
      QueryResponse response;
      try
      {
         response = solr.query(getQuery());
         results = response.getResults();
         for (SolrDocument result : results)
         {
            try
            {
               String relationship = result.getFieldValue("relationshipModel").toString();
               dv = SolrRelationshipSearchService.mapper.readValue(relationship, RelationshipDV.class);
               relationships.add(RelationshipDV.instantiate(dv, typeReg));
            }
            catch (IOException e)
            {
               logger.log(Level.SEVERE, "Failed to parse relationship record: [" + relationshipJson + "]. " + e);
            }
            catch (RelationshipException e)
            {
               logger.log(Level.SEVERE, "Error occurred while instantiating the relationship: [" + dv.id + "]. " + e);
            }
         }
      }
      catch (SolrServerException e1)
      {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }

      return relationships;
   }

   public SolrQuery getQuery()
   {
      String queryString = Joiner.on(" AND ").join(criteria);
      query.setQuery(queryString);
      return query;
   }

   @Override
   public RelationshipQueryCommand forEntity(URI entity, RelationshipDirection direction)
   {
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
      return this;
   }

   @Override
   public RelationshipQueryCommand forEntity(URI entity)
   {
      return forEntity(entity, RelationshipDirection.any);
   }

   @Override
   public RelationshipQueryCommand byType(String typeId)
   {
      criteria.add("relationshipType:\"" + typeId + "\"");
      return this;
   }

   @Override
   public RelationshipQueryCommand setRowLimit(int rows)
   {
      query.setRows(Integer.valueOf(rows));
      return this;
   }

   // TODO: decide on the proper way to organize the results
   @Override
   public RelationshipQueryCommand oderBy()
   {
      return this;
   }

}
