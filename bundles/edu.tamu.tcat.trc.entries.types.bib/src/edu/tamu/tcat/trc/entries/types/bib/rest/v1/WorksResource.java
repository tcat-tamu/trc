package edu.tamu.tcat.trc.entries.types.bib.rest.v1;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.InvalidDataException;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.types.bib.Work;
import edu.tamu.tcat.trc.entries.types.bib.dto.WorkDV;
import edu.tamu.tcat.trc.entries.types.bib.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.bib.repo.WorkRepository;
import edu.tamu.tcat.trc.entries.types.bib.search.WorkQueryCommand;
import edu.tamu.tcat.trc.entries.types.bib.search.WorkSearchProxy;
import edu.tamu.tcat.trc.entries.types.bib.search.WorkSearchService;

@Path("/works")
public class WorksResource
{
   private static final Logger logger = Logger.getLogger(WorksResource.class.getName());
//   private ConfigurationProperties properties;
   private WorkRepository repo;
   private WorkSearchService workSearchService;

   public WorksResource()
   {
   }

   // called by DS
   public void setConfigurationProperties(ConfigurationProperties properties)
   {
//      this.properties = properties;
   }

   // called by DS
   public void setRepository(WorkRepository repo)
   {
      this.repo = repo;
   }

   public void setWorkService(WorkSearchService svc)
   {
      this.workSearchService = svc;

   }

   // called by DS
   public void activate()
   {
   }

   // called by DS
   public void dispose()
   {
   }

   /**
    * Perform a "basic" search. This is a search with a single string that is matched
    * against various fields.
    *
    * @param q The basic search criteria.
    * @param numResults
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.WorkSearchResultSet
   searchWorks(@QueryParam(value = "q") String q,
               @QueryParam(value = "a") String authorName,
               @QueryParam(value = "t") String title,
               @QueryParam(value = "aid") List<String> authorIds, // specify same param with multiple values to get a list
               @QueryParam(value = "drs") List<String> dateRangeStarts,
               @QueryParam(value = "dre") List<String> dateRangeEnds,
               @QueryParam(value = "off") @DefaultValue("0")   int offset,
               @QueryParam(value = "max") @DefaultValue("100") int numResults)
   throws SearchException
   {
      WorkQueryCommand cmd = workSearchService.createQueryCommand();
      // query parameters
      cmd.query(q);
      cmd.queryAuthorName(authorName);
      cmd.queryTitle(title);

      // now filters/facets
      cmd.filterAuthor(authorIds);

      // now meta fields
      cmd.setMaxResults(numResults);
      cmd.setStartIndex(offset);

      // if either is non-empty, need to process date range criteria
      if (!dateRangeStarts.isEmpty() || !dateRangeEnds.isEmpty())
      {
         if (dateRangeStarts.size() != dateRangeEnds.size())
            throw new SearchException("Date range start or end missing a value");

         for (int i=0; i<dateRangeStarts.size(); ++i)
         {
            String st = dateRangeStarts.get(i);
            String end = dateRangeEnds.get(i);

            try
            {
               //TODO: parse start and end into a 'range' of DateDescriptor
               //TODO: allow start or end to be empty-string for unbounded query
            }
            catch (Exception e)
            {
            }
         }
      }

      RestApiV1.WorkSearchResultSet rs = new RestApiV1.WorkSearchResultSet();
      rs.items = SearchAdapter.toDTO(cmd.execute());
//      rs.items = new ArrayList<>();

      StringBuilder sb = new StringBuilder();
      try
      {
         app(sb, "q", q);
         app(sb, "a", authorName);
         app(sb, "t", title);

         authorIds.stream().forEach(s -> app(sb, "aid", s));

         // merge "start" and "end" values to be in adjacent pairs
         for (int i=0; i<dateRangeStarts.size(); ++i)
         {
            app(sb, "drs", dateRangeStarts.get(i));
            app(sb, "dre", dateRangeEnds.get(i));
         }
      }
      catch (Exception e)
      {
         throw new SearchException("Failed building querystring", e);
      }

      rs.qs = "off="+offset+"&max="+numResults+"&"+sb.toString();
      //TODO: does this depend on the number of results returned (i.e. whether < numResults), or do we assume there are infinite results?
      rs.qsNext = "off="+(offset + numResults)+"&max="+numResults+"&"+sb.toString();
      if (offset >= numResults)
         rs.qsPrev = "off="+(offset - numResults)+"&max="+numResults+"&"+sb.toString();
      // first page got off; reset to zero offset
      else if (numResults - offset > 0)
         rs.qsPrev = "off="+(0)+"&max="+numResults+"&"+sb.toString();

      return rs;
   }

   private static void app(StringBuilder sb, String p, String v)
   {
      if (v == null)
         return;
      if (sb.length() > 0)
         sb.append("&");
      try {
         sb.append(p).append("=").append(URLEncoder.encode(v, "UTF-8"));
         // suppress exception so this method can be used in lambdas
      } catch (UnsupportedEncodingException e) {
         throw new IllegalArgumentException("Failed encoding ["+v+"]", e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.WorkId createWork(WorkDV work) throws InterruptedException, ExecutionException
   {
      EditWorkCommand workCommand = repo.create();
      workCommand.setAll(work);
      RestApiV1.WorkId wid = new RestApiV1.WorkId();
      wid.id = workCommand.execute().get();
      return wid;
   }

   @PUT
   @Path("{workid}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.WorkId updateWork(@PathParam(value = "workid") String workId, RestApiV1.Work work)
   {
      try
      {
         EditWorkCommand workCommand = repo.edit(workId);
         workCommand.setAll(RepoAdapter.toRepo(work));
         RestApiV1.WorkId wid = new RestApiV1.WorkId();
         wid.id = workCommand.execute().get();
         return wid;
      }
      catch (NoSuchCatalogRecordException ex)
      {
         String msg = "Cannot update bibliographic entry [" + workId + "]. The identified entry does not exist.";
         logger.log(Level.WARNING, msg, ex);
         throw new NotFoundException(msg);
      }
      catch (InvalidDataException ex)
      {
         String msg = "Cannot update bibliographic entry [" + workId + "]. The supplied data is invalid: " + ex.getMessage();
         logger.log(Level.WARNING, msg, ex);
         throw new BadRequestException(msg);
      }

      catch (Exception ex)
      {
         String msg = "Cannot update bibliographic entry [" + workId + "]. An internal error occurred. Please see server log for full details.";
         logger.log(Level.SEVERE, msg, ex);
         throw new InternalServerErrorException(msg);
      }
   }

   @DELETE
   @Path("{workid}")
   public void deleteWork(@PathParam(value = "workid") String workId)
   {
      // FIXME handle response correctly
      repo.delete(workId);
   }

   @GET
   @Path("{workid}")
   @Produces(MediaType.APPLICATION_JSON)
   public WorkDV getWork(@PathParam(value = "workid") String id) throws NoSuchCatalogRecordException
   {
      Work w = repo.getWork(id);
      return WorkDV.create(w);
   }

   @GET
   @Path("{id}.json")
   @Produces(MediaType.APPLICATION_JSON)
   public Map<String, Integer> getWorkAsJson(@PathParam(value = "id") int id)
   {
      Map<String, Integer> result = new HashMap<>();
      result.put("id", Integer.valueOf(id));
      return result;
   }
}
