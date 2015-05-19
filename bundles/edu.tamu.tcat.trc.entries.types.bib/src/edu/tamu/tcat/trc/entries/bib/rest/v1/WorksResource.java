package edu.tamu.tcat.trc.entries.bib.rest.v1;

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

import edu.tamu.tcat.catalogentries.InvalidDataException;
import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.bib.EditWorkCommand;
import edu.tamu.tcat.trc.entries.bib.Work;
import edu.tamu.tcat.trc.entries.bib.WorkRepository;
import edu.tamu.tcat.trc.entries.bib.dto.CustomResultsDV;
import edu.tamu.tcat.trc.entries.bib.dto.WorkDV;
import edu.tamu.tcat.trc.entries.bib.search.WorkQueryCommand;
import edu.tamu.tcat.trc.entries.bib.search.WorkSearchProxy;
import edu.tamu.tcat.trc.entries.bib.search.WorkSearchService;

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

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<WorkSearchProxy> findByTitle(@QueryParam(value = "q") String q,
                                            @DefaultValue("100") @QueryParam(value = "numResults") int numResults)
   {

      WorkQueryCommand workCommand = workSearchService.createQueryCommand();
      workCommand.setQuery(q);
      workCommand.setMaxResults(numResults);
      return workCommand.execute().listItems();
   }

//   @GET
//   @Produces(MediaType.APPLICATION_JSON)
//   public List<SimpleWorkDV> listWorks(@Context UriInfo ctx) throws JsonException
//   {
//      MultivaluedMap<String, String> queryParams = ctx.getQueryParameters();
//      WorksController controller = new WorksController();
//      // TODO need to add slicing/paging support
//      // TODO add mappers for exceptions. CatalogRepoException should map to internal error.
//      return Collections.unmodifiableList(controller.query(queryParams));
//   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public CustomResultsDV createWork(WorkDV work) throws InterruptedException, ExecutionException
   {
      EditWorkCommand workCommand = repo.create();
      workCommand.setAll(work);
      String id = workCommand.execute().get();
      return new CustomResultsDV(id);
   }

   @PUT
   @Path("{workid}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public CustomResultsDV updateWork(@PathParam(value = "workid") String workId, WorkDV work)
   {
      try
      {
         EditWorkCommand workCommand = repo.edit(workId);
         workCommand.setAll(work);
         String resultId = workCommand.execute().get();
         return new CustomResultsDV(resultId);
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
