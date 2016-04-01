package edu.tamu.tcat.trc.entries.types.biblio.rest.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDV;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;
import edu.tamu.tcat.trc.entries.types.biblio.search.BiblioSearchProxy;
import edu.tamu.tcat.trc.entries.types.biblio.search.SearchWorksResult;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkQueryCommand;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkSearchService;
import edu.tamu.tcat.trc.search.SearchException;

public class WorkCollectionResource
{
   private static final Logger logger = Logger.getLogger(WorkCollectionResource.class.getName());

   private final WorkRepository repo;
   private final WorkSearchService workSearchService;

   public WorkCollectionResource(WorkRepository repo, WorkSearchService searchSvc)
   {
      this.repo = repo;
      this.workSearchService = searchSvc;
   }

   /**
    * Perform a "basic" search. This is a search with a single string that is matched
    * against various fields.
    *
    * @param query The basic search criteria.
    * @param authorNames Each "a" argument is an individual literal; items in the list are combined via "OR" criteria
    * @param titles Each "t" argument is an individual literal; items in the list are combined via "OR" criteria
    * @param numResults
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.WorkSearchResultSet
   searchWorks(@QueryParam(value = "q") String query,
               @QueryParam(value = "a") List<String> authorNames,
               @QueryParam(value = "t") List<String> titles,
               @QueryParam(value = "aid") List<String> authorIds, // specify same param with multiple values to get a list
               @QueryParam(value = "dr") List<RestApiV1.DateRangeParam> dateRanges,
               @QueryParam(value = "off") @DefaultValue("0")   int offset,
               @QueryParam(value = "max") @DefaultValue("100") int numResults)
   {
      SearchWorksResult worksResult;

      try
      {
         WorkQueryCommand cmd = workSearchService.createQueryCommand();

         // First, set query parameters
         //NOTE: a query can work without any parameters, so no need to validate that at least a 'q'
         //      or other advanced criteria was supplied
         if (query != null)
         {
            cmd.query(query);
         }

         for (String n : authorNames)
         {
            cmd.queryAuthorName(n);
         }

         for (String t : titles)
         {
            cmd.queryTitle(t);
         }

         // now filters/facets
         cmd.addFilterAuthor(authorIds);
         for (RestApiV1.DateRangeParam dr : dateRanges)
         {
            cmd.addFilterDate(dr.start, dr.end);
         }

         // now meta fields
         cmd.setMaxResults(numResults);
         cmd.setOffset(offset);

         // execute query
         worksResult = cmd.execute();
      }
      catch (SearchException e)
      {
         String message = "Unable to search for works.";
         logger.log(Level.SEVERE, message, e);
         throw new InternalServerErrorException(message, e);
      }


      // assemble search response data vehicle
      RestApiV1.WorkSearchResultSet resultSet = new RestApiV1.WorkSearchResultSet();
      List<BiblioSearchProxy> results = worksResult.get();
      resultSet.items = SearchAdapter.toDTO(results);

      QueryStringBuilder qsbCommon = new QueryStringBuilder();

      // query parameters common to current/next/previous queries
      qsbCommon.add("q", query);
      authorNames.stream().forEach(name -> qsbCommon.add("a", name));
      titles.stream().forEach(title -> qsbCommon.add("t", title));
      authorIds.stream().forEach(authorId -> qsbCommon.add("aid", authorId));
      dateRanges.stream().forEach(dateRange -> qsbCommon.add("dr", dateRange.toValue()));
      qsbCommon.add("max", numResults);

      // query parameters specific to the current query
      QueryStringBuilder qsbCurrent = new QueryStringBuilder(qsbCommon);
      qsbCurrent.add("off", offset);
      resultSet.qs = qsbCurrent.toString();

      // query parameters specific to the next query
      QueryStringBuilder qsbNext = new QueryStringBuilder(qsbCommon);
      qsbNext.add("off", offset + numResults);
      resultSet.qsNext = qsbNext.toString();

      // query parameters specific to the previous query
      // only include previous query if available
      if (offset > 0)
      {
         QueryStringBuilder qsbPrev = new QueryStringBuilder(qsbCommon);
         qsbPrev.add("off", Math.max(0, offset - numResults));
         resultSet.qsPrev = qsbPrev.toString();
      }

      return resultSet;
   }

   /**
    * Saves a new work object
    *
    * @param work
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.WorkId createWork(RestApiV1.Work work)
   {
      EditWorkCommand workCommand = repo.create();
      WorkDV repoDto = RepoAdapter.toRepo(work);
      workCommand.setAll(repoDto);

      try
      {
         RestApiV1.WorkId wid = new RestApiV1.WorkId();
         wid.id = workCommand.execute().get();
         return wid;
      }
      catch (Exception e)
      {
         String message = "Unable to save new work.";
         logger.log(Level.SEVERE, message, e);
         throw new InternalServerErrorException(message, e);
      }
   }

   /**
    * Perform operations on an individual work
    *
    * @param id
    * @return JAX-RS subresource for the work
    */
   @Path("/{id}")
   public WorkResource getWork(@PathParam("id") String id)
   {
      return new WorkResource(id, repo);
   }

   /**
    * Query string builder utility class
    *
    * Should probably be refactored into its own class somewhere
    */
   private static class QueryStringBuilder
   {
      private static final String ENCODING = "UTF-8";

      private List<NameValuePair> params;

      /**
       * Default constructor
       */
      public QueryStringBuilder()
      {
         params = new ArrayList<>();
      }

      /**
       * Copy constructor
       *
       * @param original
       */
      public QueryStringBuilder(QueryStringBuilder original)
      {
         params = new ArrayList<>(original.params);
      }

      /**
       * Adds a param/value entry to the query string.
       * Multiple entries of the same parameter are allowed.
       *
       * @param param
       * @param value
       */
      public void add(String param, Object value)
      {
         params.add(new BasicNameValuePair(param, String.valueOf(value)));
      }

      /**
       * Build the final query string
       */
      @Override
      public String toString()
      {
         return URLEncodedUtils.format(params, ENCODING);
      }
   }

}