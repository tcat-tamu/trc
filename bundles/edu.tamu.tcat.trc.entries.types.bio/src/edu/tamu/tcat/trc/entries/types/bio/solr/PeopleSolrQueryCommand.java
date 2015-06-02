package edu.tamu.tcat.trc.entries.types.bio.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.search.solr.SolrQueryBuilder;
import edu.tamu.tcat.trc.entries.types.bio.search.BioSearchProxy;
import edu.tamu.tcat.trc.entries.types.bio.search.PeopleQueryCommand;

public class PeopleSolrQueryCommand implements PeopleQueryCommand
{
   private final static Logger logger = Logger.getLogger(PeopleSolrQueryCommand.class.getName());

   private static final int DEFAULT_MAX_RESULTS = 25;

   private final SolrServer solr;
   private final SolrQueryBuilder qb;

   public PeopleSolrQueryCommand(SolrServer solr, SolrQueryBuilder qb)
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

         List<BioSearchProxy> people = new ArrayList<>();
         for (SolrDocument doc : results)
         {
            String person = doc.getFieldValue(BioSolrConfig.SEARCH_PROXY.getName()).toString();
            BioSearchProxy simplePerson = PeopleIndexingService.mapper.readValue(person, BioSearchProxy.class);
            people.add(simplePerson);
         }

         return new SolrPersonResults(this, people);
      }
      catch (SolrServerException | IOException sse)
      {
         throw new SearchException("An error occurred while querying the author core: " + sse, sse);
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
