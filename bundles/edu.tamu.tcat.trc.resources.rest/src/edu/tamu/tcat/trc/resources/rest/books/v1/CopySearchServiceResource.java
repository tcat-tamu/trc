package edu.tamu.tcat.trc.resources.rest.books.v1;

import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.logging.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.resources.books.discovery.ContentQuery;
import edu.tamu.tcat.trc.resources.books.discovery.CopySearchResult;
import edu.tamu.tcat.trc.resources.books.discovery.CopySearchService;
import edu.tamu.tcat.trc.resources.books.hathitrust.HTFilesSearchService;


@Path("/resources/books/search")
public class CopySearchServiceResource
{
   private static final Logger logger = Logger.getLogger(CopySearchServiceResource.class.getName());

   private CopySearchService searchService;

   // called by DS
   public void setRepository(CopySearchService svc)
   {
      this.searchService = svc;
   }

   // called by DS
   public void activate()
   {
      if (this.searchService == null)
      {
         // HACK: auto generate HathiTrust search service
         this.searchService = new HTFilesSearchService();
      }
   }

   // called by DS
   public void dispose()
   {
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public SearchResult search(@QueryParam(value = "q") String q,
                              @QueryParam(value = "author") String author,
                              @DefaultValue("-9999") @QueryParam(value = "after") int after,
                              @DefaultValue("-9999") @QueryParam(value = "before") int before,
                              @DefaultValue("0") @QueryParam(value = "offset") int offset,
                              @DefaultValue("25") @QueryParam(value = "limit") int limit
                              )
   {
      Year beforeYr = (before != -9999) ?  Year.of(before) : null;
      Year afterYr = (after != -9999) ?  Year.of(after) : null;
      CopyQueryImpl query = new CopyQueryImpl(q, author, afterYr, beforeYr, offset, limit);

      return executeQuery(query);
   }

   private SearchResult executeQuery(CopyQueryImpl query)
   {
      try
      {
         CopySearchResult result = searchService.find(query);

         CopyQueryDTO qdto = CopyQueryDTO.create(query, DateTimeFormatter.ofPattern("yyyy"));
         return new SearchResult(result, qdto);
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Failed to execute query [" + query + "]", ex);
      }
   }

   public static class CopyQueryImpl implements ContentQuery
   {
      public String q;
      public String author;
      public Year after;
      public Year before;
      public int offset;
      public int limit;

      public CopyQueryImpl(String keyWords, String author, Year after, Year before, int offset, int limit)
      {
         this.q = keyWords;
         this.author = author;
         this.before = before;
         this.after = after;
         this.offset = offset;
         this.limit = limit;
      }

      @Override
      public String getKeyWordQuery()
      {
         return q;
      }

      @Override
      public String getAuthorQuery()
      {
         return author;
      }

      @Override
      public TemporalAccessor getDateRangeStart()
      {
         return after;
      }

      @Override
      public TemporalAccessor getDateRangeEnd()
      {
         return before;
      }

      @Override
      public int getOffset()
      {
         return offset;
      }

      @Override
      public int getLimit()
      {
         return limit;
      }
   }
}
