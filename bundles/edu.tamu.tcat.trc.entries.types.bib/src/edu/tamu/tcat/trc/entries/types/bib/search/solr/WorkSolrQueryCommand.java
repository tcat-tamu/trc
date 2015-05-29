package edu.tamu.tcat.trc.entries.types.bib.search.solr;

import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;

import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.types.bib.search.WorkQueryCommand;
import edu.tamu.tcat.trc.entries.types.bib.search.WorkSearchProxy;

public class WorkSolrQueryCommand implements WorkQueryCommand
{
   private final static Logger logger = Logger.getLogger(WorkSolrQueryCommand.class.getName());

   // Solr field name values for works

   private SolrServer solr;

   private int start = 0;
   private int maxResults = 25;
   private String qBasic;

   private Set<String> authorIds;
   private List<DateRange> dates;

//   private String advTitle;
//   private String advAuthorName;
//   private String location;

   public WorkSolrQueryCommand(SolrServer solr)
   {
      this.solr = solr;
   }

   @Override
   public SolrWorksResults execute()
   {
      List<WorkSearchProxy> works = new ArrayList<>();

      try
      {
         QueryResponse response = solr.query(getQuery());
         SolrDocumentList results = response.getResults();

         for (SolrDocument doc : results)
         {
            String workInfo = null;
            try
            {
               workInfo = doc.getFieldValue("workInfo").toString();
               WorkSearchProxy wi = BiblioEntriesSearchService.getMapper().readValue(workInfo, WorkSearchProxy.class);
               works.add(wi);
            }
            catch (IOException ioe)
            {
               logger.log(Level.SEVERE, "Failed to parse relationship record: [" + workInfo + "]. " + ioe);
            }
         }
      }
      catch (SolrServerException e)
      {
         //TODO: this should throw instead of log a failure and return incomplete results; let
         //      the caller handle that there was an exception
         logger.log(Level.SEVERE, "The following error occurred while querying the works core :" + e);
      }

      return new SolrWorksResults(this, works);
   }

   private SolrParams getQuery()
   {
      /*
       * This is a proof of concept:
       * Using eDisMax seemed like a more adventagous way of doing the query. This will allow
       * additional solr Paramaters to be set in order to 'fine tune' the query.
       */
      ModifiableSolrParams solrParams = new ModifiableSolrParams();

      //HACK: if no query specified, should this throw and require a call to queryAll() ?
      if (qBasic == null || qBasic.trim().isEmpty())
         qBasic = "*:*";

      // NOTE query against all fields, boosted appropriately, free text
      //      I think that means *:(qBasic)
      // NOTE in general, if this is applied, the other query params are unlikely to be applied
      StringBuilder qBuilder = new StringBuilder(qBasic);
      qBuilder.append(" -editionName:(*)")
              .append(" -volumeNumber:(*)");

      solrParams.set("q", qBuilder.toString());

      solrParams.set("defType", "edismax");
      solrParams.set("qf", "titles^3 authorNames authorIds");
      solrParams.set("start", start);
      solrParams.set("rows", this.maxResults);

      return solrParams;
   }

   @Override
   public void query(String q)
   {
      this.qBasic = q;
   }

   public void queryAll()
   {
      qBasic = "*:*";
   }

   @Override
   public void queryTitle(String q)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void queryAuthorName(String authorName)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void addFilterAuthor(Collection<String> authorIds) throws SearchException
   {
      if (this.authorIds == null)
         this.authorIds = new HashSet<>();
      if (authorIds != null)
         this.authorIds.addAll(authorIds);
   }

   @Override
   public void clearFilterAuthor() throws SearchException
   {
      this.authorIds = null;
   }

   @Override
   public void addFilterDate(Year start, Year end) throws SearchException
   {
      if (dates == null)
         dates = new ArrayList<>();

      //TODO: validate date range overlaps
      dates.add(new DateRange(start, end));
   }

   @Override
   public void clearFilterDate() throws SearchException
   {
      dates = null;
   }

//   @Override
//   public void setTitleQuery(String qBasic)
//   {
//      this.titleQuery = qBasic;
//   }
//
//   @Override
//   public void setAuthorName(String authorName)
//   {
//      this.authorName = authorName;
////      criteria.add("authorNames\"" + authorName + "\"");
//   }
//
//   @Override
//   public void filterByAuthor(String... ids)
//   {
//      // NOTE these should be joined by OR's
//      this.authorIds = ids;
//   }
//
//   @Override
//   public void filterByLocation(String location)
//   {
//      this.location = location;
//   }

   @Override
   public void setStartIndex(int start)
   {
      this.start = start;
   }

   @Override
   public void setMaxResults(int max)
   {
      this.maxResults = max;
   }

   //TODO: add constructors and fields to support unbounded date ranges
   private static class DateRange
   {
      public final Year start;
      public final Year end;

      public DateRange(Year s, Year e)
      {
         start = Objects.requireNonNull(s, "Start date may not be null");
         end = Objects.requireNonNull(e, "End date may not be null");
      }
   }
}
