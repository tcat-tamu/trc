package edu.tamu.tcat.trc.entries.types.bio.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.google.common.base.Joiner;

import edu.tamu.tcat.trc.entries.types.bio.rest.v1.SimplePersonResultDV;
import edu.tamu.tcat.trc.entries.types.bio.search.PeopleQueryCommand;

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
   public List<SimplePersonResultDV> getResults() throws Exception
   {
      try
      {
         QueryResponse response = solr.query(getQuery());
         SolrDocumentList results = response.getResults();

         List<SimplePersonResultDV> people = new ArrayList<>();
         for (SolrDocument result : results)
         {
            String person = result.getFieldValue(personInfo).toString();
            SimplePersonResultDV simplePerson = PeopleIndexingService.mapper.readValue(person, SimplePersonResultDV.class);
            people.add(simplePerson);
         }
         
         return people;
      }
      catch (SolrServerException | IOException sse)
      {
         throw new Exception("The following error occurred while querying the author core :" + sse, sse);
      }
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
