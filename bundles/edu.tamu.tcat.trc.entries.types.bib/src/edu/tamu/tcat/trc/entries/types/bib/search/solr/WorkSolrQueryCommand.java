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

import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.search.solr.SolrQueryBuilder;
import edu.tamu.tcat.trc.entries.types.bib.search.WorkQueryCommand;
import edu.tamu.tcat.trc.entries.types.bib.search.WorkSearchProxy;

public class WorkSolrQueryCommand implements WorkQueryCommand
{
   private final static Logger logger = Logger.getLogger(WorkSolrQueryCommand.class.getName());

   private static final int DEFAULT_MAX_RESULTS = 25;

   // Solr field name values for works

   private final SolrServer solr;
   private final SolrQueryBuilder qb;

//   private int start = 0;
//   private int maxResults = 25;
//   private String qBasic;

   private Set<String> authorIds;
   private List<DateRange> dates;


//   private String advTitle;
//   private String advAuthorName;
//   private String location;

   public WorkSolrQueryCommand(SolrServer solr, SolrQueryBuilder qb)
   {
      this.solr = solr;
      this.qb = qb;
      qb.max(DEFAULT_MAX_RESULTS);
   }

   @Override
   public SolrWorksResults execute() throws SearchException
   {
      List<WorkSearchProxy> works = new ArrayList<>();

      try
      {
         if (authorIds != null)
         {
            //TODO: specify in query builder
         }
         if (dates != null)
         {
            //TODO: specify in query builder
         }

         QueryResponse response = solr.query(qb.get());
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
         throw new SearchException("An error occurred while querying the works core", e);
      }

      return new SolrWorksResults(this, works);
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
   public void queryTitle(String q)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void queryAuthorName(String authorName) throws SearchException
   {
      qb.query(BiblioSolrConfig.AUTHOR_NAME, authorName);
   }

   @Override
   public void addFilterAuthor(Collection<String> ids) throws SearchException
   {
      if (this.authorIds == null)
         this.authorIds = new HashSet<>();
      if (authorIds != null)
         this.authorIds.addAll(ids);
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

   @Override
   public void setOffset(int start)
   {
      if (start < 0)
         throw new IllegalArgumentException("Offset cannot be negative");

      qb.offset(start);
   }

   @Override
   public void setMaxResults(int max)
   {
      qb.max(max);
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
