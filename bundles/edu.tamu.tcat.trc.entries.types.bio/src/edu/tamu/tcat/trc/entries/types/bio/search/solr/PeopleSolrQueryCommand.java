package edu.tamu.tcat.trc.entries.types.bio.search.solr;

import java.util.List;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.search.solr.impl.TrcQueryBuilder;
import edu.tamu.tcat.trc.entries.types.bio.search.BioSearchProxy;
import edu.tamu.tcat.trc.entries.types.bio.search.PeopleQueryCommand;

public class PeopleSolrQueryCommand implements PeopleQueryCommand
{
   private final static Logger logger = Logger.getLogger(PeopleSolrQueryCommand.class.getName());

   private static final int DEFAULT_MAX_RESULTS = 25;

   private final SolrServer solr;
   private final TrcQueryBuilder qb;

   public PeopleSolrQueryCommand(SolrServer solr, TrcQueryBuilder qb)
   {
      this.solr = solr;
      this.qb = qb;
      qb.max(DEFAULT_MAX_RESULTS);
   }

   @Override
   public SolrPersonResults execute() throws SearchException
   {
      try
      {
         QueryResponse response = solr.query(qb.get());
         SolrDocumentList results = response.getResults();

         List<BioSearchProxy> people = qb.unpack(results, BioSolrConfig.SEARCH_PROXY);
         return new SolrPersonResults(this, people);
      }
      catch (Exception e)
      {
         throw new SearchException("An error occurred while querying the author core: " + e, e);
      }
   }

   @Override
   public void query(String q) throws SearchException
   {
      qb.basic(q);
   }

   public void queryAll() throws SearchException
   {
      qb.basic("*:*");
   }

   @Override
   public void queryFamilyName(String familyName) throws SearchException
   {
      // Add quotes so each term acts as a literal
      qb.query(BioSolrConfig.FAMILY_NAME, '"' + familyName + '"');
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
