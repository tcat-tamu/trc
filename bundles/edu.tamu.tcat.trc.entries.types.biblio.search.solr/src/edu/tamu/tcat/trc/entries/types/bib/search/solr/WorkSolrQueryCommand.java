package edu.tamu.tcat.trc.entries.types.bib.search.solr;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import edu.tamu.tcat.trc.entries.types.bib.search.BiblioSearchProxy;
import edu.tamu.tcat.trc.entries.types.bib.search.WorkQueryCommand;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.DateRangeDTO;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;

public class WorkSolrQueryCommand implements WorkQueryCommand
{
   private static final int DEFAULT_MAX_RESULTS = 25;

   private final SolrServer solr;
   private final TrcQueryBuilder qb;

   private Set<String> authorIds;
   private List<DateRangeDTO> dates;

   public WorkSolrQueryCommand(SolrServer solr, TrcQueryBuilder qb)
   {
      this.solr = solr;
      this.qb = qb;
      qb.max(DEFAULT_MAX_RESULTS);
   }

   @Override
   public SolrWorksResults execute() throws SearchException
   {
      try
      {
         if (authorIds != null)
         {
            //TODO: specify in query builder
         }
         if (dates != null)
         {
            for (DateRangeDTO dr : dates)
               qb.filterRange(BiblioSolrConfig.PUBLICATION_DATE,
                              // For a "year" query, search from 1 Jan of start year through 31 Dec of end year (inclusive)
                              LocalDate.of(dr.start.getValue(), Month.JANUARY, 1),
                              LocalDate.of(dr.end.getValue(), Month.DECEMBER, 31));
         }

         QueryResponse response = solr.query(qb.get());
         SolrDocumentList results = response.getResults();

         List<BiblioSearchProxy> works = qb.unpack(results, BiblioSolrConfig.SEARCH_PROXY);
         return new SolrWorksResults(this, works);
      }
      catch (SolrServerException e)
      {
         throw new SearchException("An error occurred while querying the works core", e);
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
   public void queryTitle(String title) throws SearchException
   {
      // Add quotes so each term acts as a literal
      qb.query(BiblioSolrConfig.TITLES, '"' + title + '"');
   }

   @Override
   public void queryAuthorName(String authorName) throws SearchException
   {
      // Add quotes so each term acts as a literal
      qb.query(BiblioSolrConfig.AUTHOR_NAMES, '"' + authorName + '"' );
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
      dates.add(new DateRangeDTO(start, end));
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
         throw new IllegalArgumentException("Offset ["+start+"] cannot be negative");

      qb.offset(start);
   }

   @Override
   public void setMaxResults(int max)
   {
      qb.max(max);
   }
}
