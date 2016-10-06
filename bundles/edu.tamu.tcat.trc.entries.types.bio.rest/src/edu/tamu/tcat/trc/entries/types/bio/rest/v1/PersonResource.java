package edu.tamu.tcat.trc.entries.types.bio.rest.v1;

import static java.text.MessageFormat.format;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.trc.entries.core.repo.NoSuchEntryException;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditBiographicalEntryCommand;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.internal.ApiUtils;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.internal.RepoAdapter;
import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.resolver.EntryResolver;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.services.TrcServiceManager;
import edu.tamu.tcat.trc.services.bibref.repo.RefCollectionService;
import edu.tamu.tcat.trc.services.rest.bibref.ReferenceCollectionResource;

public class PersonResource
{
   private static final Logger logger = Logger.getLogger(PersonResource.class.getName());

   private final String personId;
   private final BiographicalEntryRepository repo;
   private final TrcServiceManager serviceManager;
   private final EntryResolverRegistry resolverRegistry;

   public PersonResource(BiographicalEntryRepository repo, String personId, TrcServiceManager serviceManager, EntryResolverRegistry resolverRegistry)
   {
      this.repo = repo;
      this.personId = personId;
      this.serviceManager = serviceManager;
      this.resolverRegistry = resolverRegistry;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Person getPerson()
   {
      try
      {
         BiographicalEntry figure = repo.get(personId);
         return RepoAdapter.toDTO(figure);
      }
      catch (NoSuchEntryException e)
      {
         throw new NotFoundException("No biographical entry is available " + personId);
      }
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Person updatePerson(RestApiV1.Person person)
   {
      if (person.id == null)
         person.id = personId;

      if (!Objects.equals(personId, person.id))
         throw new BadRequestException("The id of the supplied person data does not match the URL");

      try
      {
         EditBiographicalEntryCommand command = repo.edit(person.id);
         PeopleResource.apply(command, person);

         logger.log(Level.INFO, format("Updating biographic entry for {0} [{1}]", person.name.label, personId));
         return PeopleResource.execute(repo, command);
      }
      catch (NoSuchEntryException ex)
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
         repo.remove(personId).get(10, TimeUnit.SECONDS);
      }
      catch (InterruptedException | TimeoutException e)
      {
         String msg = "Oops. Things seem to be a bit busy now and we could not delete the bibliographic entry {0} in a timely manner. Please try again.";
         throw ApiUtils.raise(Response.Status.SERVICE_UNAVAILABLE, format(msg, personId), Level.WARNING, e);
      }
      catch (ExecutionException e)
      {
         String msg = "Failed to delete biographic entry [{0}]";
         Throwable cause = e.getCause();
         if (!NoSuchEntryException.class.isInstance(cause))
            throw PeopleResource.handleExecutionException(format(msg,  personId), e);
      }

      return Response.noContent().build();
   }

   @GET
   @Path("references")
   public ReferenceCollectionResource getReferences()
   {
      RefCollectionService refsService = serviceManager.getService(RefCollectionService.makeContext(null));

      EntryReference reference = repo.getOptionally(personId)
            .map(person -> {
               EntryResolver<BiographicalEntry> resolver = resolverRegistry.getResolver(person);
               return resolver.makeReference(person);
            })
            .orElseThrow(() -> ApiUtils.raise(Response.Status.INTERNAL_SERVER_ERROR, format("Unable to generate reference for {0}", personId), Level.SEVERE, null));

      return new ReferenceCollectionResource(refsService, reference);
   }
}
