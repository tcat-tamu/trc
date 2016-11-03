package edu.tamu.tcat.trc.entries.types.bio.rest.v1;

import static java.text.MessageFormat.format;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.trc.EntryFacade;
import edu.tamu.tcat.trc.ResourceNotFoundException;
import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditBiographicalEntryCommand;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.internal.ApiUtils;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.internal.RepoAdapter;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.services.bibref.repo.RefCollectionService;
import edu.tamu.tcat.trc.services.rest.bibref.ReferenceCollectionResource;

public class PersonResource
{
   private static final Logger logger = Logger.getLogger(PersonResource.class.getName());

   private final TrcApplication app;
   private final EntryId entryId;

   public PersonResource(TrcApplication app, String personId)
   {
      this.app = app;
      this.entryId = new EntryId(personId, BiographicalEntryRepository.ENTRY_TYPE_ID);
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Person getPerson()
   {
      String notFoundMsg = "Cannot find a biographical entry with id={0}. This may be because the entry does not exist, "
            + "has been deleted or the current user does not have permission to access it.";
      try
      {
         EntryFacade<BiographicalEntry> facade =
               app.getEntryFacade(entryId, BiographicalEntry.class, null);
         return facade.getEntry().map(RepoAdapter::toDTO)
               .orElseThrow(() -> ApiUtils.raise(Response.Status.NOT_FOUND, format(notFoundMsg, entryId.getId()), Level.FINE, null));
      }
      catch (Exception e)
      {
         throw ApiUtils.raise(Response.Status.NOT_FOUND, "Failed to retrieve biographical entry for " + entryId.getId(), Level.SEVERE, e);
      }
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Person updatePerson(RestApiV1.Person person)
   {
      String personId = entryId.getId();
      if (person.id == null)
         person.id = personId;

      if (!Objects.equals(personId, person.id))
      {
         String errMsg = "The id of the supplied person [{0}] data does not match resource id in the URL {1}";
         throw ApiUtils.raise(Response.Status.BAD_REQUEST, format(errMsg, person.id, personId), Level.WARNING, null);
      }

      try
      {
         BiographicalEntryRepository repo = app.getRepository(null, BiographicalEntryRepository.class);
         EditBiographicalEntryCommand command = repo.edit(person.id);
         PeopleResource.apply(command, person);

         logger.log(Level.INFO, format("Updating biographic entry for {0} [{1}]", person.name.label, personId));
         return PeopleResource.execute(repo, command);
      }
      catch (ResourceNotFoundException ex)
      {
         String msg = "Cannot edit bibliographic entry for [{0}]. No record found";
         throw ApiUtils.raise(Response.Status.NOT_FOUND, format(msg, personId), Level.FINE, ex);
      }
   }

   @DELETE
   @Consumes(MediaType.APPLICATION_JSON)
   public Response deletePerson()
   {
      try
      {
         EntryFacade<BiographicalEntry> entry = app.getEntryFacade(entryId, BiographicalEntry.class, null);
         entry.remove().get(10, TimeUnit.SECONDS);
      }
      catch (InterruptedException | TimeoutException e)
      {
         String msg = "Oops. Things seem to be a bit busy now and we could not delete the bibliographic entry {0} in a timely manner. Please try again.";
         throw ApiUtils.raise(Response.Status.SERVICE_UNAVAILABLE, format(msg, entryId.getId()), Level.WARNING, e);
      }
      catch (ExecutionException e)
      {
         String msg = "Failed to delete biographic entry [{0}]";
         Throwable cause = e.getCause();
         if (!ResourceNotFoundException.class.isInstance(cause))
            throw PeopleResource.handleExecutionException(format(msg,  entryId.getId()), e);
      }

      return Response.noContent().build();
   }

   @Path("references")
   public ReferenceCollectionResource getReferences()
   {
      try
      {
         RefCollectionService refsService = app.getService(RefCollectionService.makeContext(null));
         return new ReferenceCollectionResource(refsService, entryId);
      }
      catch (Exception ex)
      {
         String format = format("Unable to generate references for {0}", entryId.getId());
         throw ApiUtils.raise(Response.Status.INTERNAL_SERVER_ERROR, format, Level.SEVERE, null);

      }
   }
}
