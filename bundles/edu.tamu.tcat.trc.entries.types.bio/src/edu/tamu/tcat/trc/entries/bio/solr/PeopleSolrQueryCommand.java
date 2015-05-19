package edu.tamu.tcat.trc.entries.bio.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.google.common.base.Joiner;

import edu.tamu.tcat.trc.entries.bio.PeopleQueryCommand;
import edu.tamu.tcat.trc.entries.bio.rest.v1.SimplePersonResultDV;

public class PeopleSolrQueryCommand implements PeopleQueryCommand
{
   private final static Logger logger = Logger.getLogger(PeopleSolrQueryCommand.class.getName());

   private final static String personInfo = "personInfo";

   private SolrQuery query = new SolrQuery();
   private Collection<String> criteria = new ArrayList<>();

   private SolrServer solr;

   public PeopleSolrQueryCommand(SolrServer solr)
   {
      this.solr = solr;
   }

   @Override
   public List<SimplePersonResultDV> getResults()
   {
      List<SimplePersonResultDV> people = new ArrayList<>();
      QueryResponse response;
      String person = "";
      SimplePersonResultDV simplePerson = new SimplePersonResultDV();

      try
      {
         response = solr.query(getQuery());
         SolrDocumentList results = response.getResults();

         for (SolrDocument result : results)
         {
            person = result.getFieldValue(personInfo).toString();
            simplePerson = PeopleIndexingService.mapper.readValue(person, SimplePersonResultDV.class);
            people.add(simplePerson);
         }
      }
      catch (SolrServerException | IOException sse)
      {
         logger.log(Level.SEVERE, "The following error occurred while querying the author core :" + sse);
      }

      return people;
   }


   public SolrQuery getQuery()
   {
      String queryString = Joiner.on(" AND ").join(criteria);
      query.setQuery(queryString);
      return query;
   }

   @Override
   public PeopleQueryCommand search(String syntheticName)
   {
      criteria.add("syntheticName:(" + syntheticName + ")");
      return this;
   }

   @Override
   public PeopleQueryCommand byFamilyName(String familyName)
   {
      criteria.add("familyName:\"" + familyName + "\"");
      return this;
   }

   @Override
   public PeopleQueryCommand setRowLimit(int rows)
   {
      query.setRows(Integer.valueOf(rows));
      return this;
   }

}
