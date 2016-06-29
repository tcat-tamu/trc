package edu.tamu.tcat.trc.entries.types.bio.rest.v1;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditPersonCommand;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.internal.RepoAdapter;
import edu.tamu.tcat.trc.repo.NoSuchEntryException;

public class PersonResource
{
   private final String personId;
   private PeopleRepository repo;

   public PersonResource(PeopleRepository repo, String personId)
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
         Person figure = repo.get(personId);
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
   public RestApiV1.PersonId updatePerson(RestApiV1.Person person)
   {
      if (person.id == null)
         person.id = this.personId;

      if (!Objects.equals(personId, person.id))
         throw new BadRequestException("The id of the supplied person data does not match the URL");

      try
      {
         EditPersonCommand updateCommand = repo.update(person.id);
         updateCommand.setAll(RepoAdapter.toRepo(person));
         updateCommand.execute().get();

         RestApiV1.PersonId result = new RestApiV1.PersonId();
         result.id = person.id;
         return result;
      }
      catch (NoSuchEntryException ex)
      {
         throw new NotFoundException(
               MessageFormat.format("Cannot update person {0}. There is no record for this id.", personId));
      }
      catch (InterruptedException | ExecutionException ex)
      {
         throw new InternalServerErrorException("Failed to update person.", ex);
      }
   }

   @DELETE
   @Consumes(MediaType.APPLICATION_JSON)
   public Response deletePerson()
   {
      try
      {
         repo.delete(personId);
      }
      catch (NoSuchEntryException ex)
      {
         // no-op.
      }

      return Response.noContent().build();
   }
}
