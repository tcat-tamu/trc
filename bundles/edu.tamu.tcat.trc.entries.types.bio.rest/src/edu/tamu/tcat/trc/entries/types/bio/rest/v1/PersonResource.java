package edu.tamu.tcat.trc.entries.types.bio.rest.v1;

import static java.text.MessageFormat.format;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.trc.entries.core.repo.NoSuchEntryException;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditBiographicalEntryCommand;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.internal.ApiUtils;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.internal.RepoAdapter;

public class PersonResource
{
   private final String personId;
   private BiographicalEntryRepository repo;

   public PersonResource(BiographicalEntryRepository repo, String personId)
   {
      this.repo = repo;
      this.personId = personId;
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
         person.id = this.personId;

      if (!Objects.equals(personId, person.id))
         throw new BadRequestException("The id of the supplied person data does not match the URL");

      try
      {
         EditBiographicalEntryCommand command = repo.edit(person.id);
         PeopleResource.apply(command, person);
         return PeopleResource.execute(repo, command, person.name.label);
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
}
