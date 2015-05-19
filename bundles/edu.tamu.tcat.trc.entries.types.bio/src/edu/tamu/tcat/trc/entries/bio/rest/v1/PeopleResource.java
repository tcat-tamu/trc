package edu.tamu.tcat.trc.entries.bio.rest.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.catalogentries.CatalogRepoException;
import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.bio.EditPeopleCommand;
import edu.tamu.tcat.trc.entries.bio.PeopleQueryCommand;
import edu.tamu.tcat.trc.entries.bio.PeopleRepository;
import edu.tamu.tcat.trc.entries.bio.PeopleSearchService;
import edu.tamu.tcat.trc.entries.bio.Person;
import edu.tamu.tcat.trc.entries.bio.dv.PersonDV;


@Path("/people")
public class PeopleResource
{
   // TODO add authentication filter in front of this call
   // TODO create PersonResource

   // records internal errors accessing the REST
   static final Logger errorLogger = Logger.getLogger(PeopleResource.class.getName());

   private PeopleRepository repo;
   private PeopleSearchService peopleSearchService;

   // called by DS
   public void setRepository(PeopleRepository repo)
   {
      this.repo = repo;
   }

   public void setPeopleService(PeopleSearchService service)
   {
      this.peopleSearchService = service;

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
   @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
   public List<SimplePersonResultDV> listPeople(@QueryParam(value="syntheticName") String prefix,
                                                @DefaultValue("50") @QueryParam(value="numResults") int numResults)

   {

      PeopleQueryCommand peopleQuery = peopleSearchService.createQueryCommand();
      peopleQuery.search(prefix);
      peopleQuery.setRowLimit(numResults);
      return Collections.unmodifiableList(peopleQuery.getResults());
//      try {
//         List<SimplePersonResultDV> results = new ArrayList<>();
//
//         Iterable<Person> people = (prefix == null) ? repo.findPeople() : repo.findByName(prefix);
//         for (Person person : people) {
//            results.add(new SimplePersonResultDV(person));
//
//            if (results.size() == numResults) {
//               break;
//            }
//         }
//
//         return results;
//      }
//      catch (CatalogRepoException e) {
//         e.printStackTrace();
//         return Collections.emptyList();
//      }
   }

   @GET
   @Path("{personId}")
   @Produces(MediaType.APPLICATION_JSON)
   public PersonDV getPerson(@PathParam(value="personId") String personId) throws NoSuchCatalogRecordException
   {
      // FIXME make this a string based identifier
      // TODO make this a mangled string instead of an ID. Don't want people guessing
      //      unique identifiers
      // TODO add mappers for exceptions.
      //       CatalogRepoException should map to internal error.
      //       NoSuchCatalogRecordException should map to 404
      Person figure = repo.get(personId);
      return PersonDV.create(figure);
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public PersonId createPerson(PersonDV person) throws Exception
   {
      PersonId personId = new PersonId();
      EditPeopleCommand createCommand = repo.create();

      createCommand.setAll(person);
      String id = createCommand.execute().get();
      personId.id = id;
      return personId;
   }

   @PUT
   @Path("{personId}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public PersonId updatePerson(PersonDV person) throws Exception
   {
      PersonId personId = new PersonId();
      EditPeopleCommand updateCommand = repo.update(person);
      updateCommand.execute().get();
      personId.id = person.id;
      return personId;
   }

   @DELETE
   @Path("{personId}")
   @Consumes(MediaType.APPLICATION_JSON)
   public void deletePerson(@PathParam(value="personId") String personId) throws Exception
   {
      EditPeopleCommand deleteCommand = repo.delete(personId);
      deleteCommand.execute();
   }


   /**
    * Wrapper to format JSON results
    */
   public class PersonId
   {
      public String id;
   }
}
