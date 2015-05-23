package edu.tamu.tcat.trc.entries.types.bio.rest.v1;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

import edu.tamu.tcat.catalogentries.events.dv.DateDescriptionDV;
import edu.tamu.tcat.catalogentries.events.dv.HistoricalEventDV;
import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.common.HistoricalEvent;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.entries.types.bio.dto.PersonDTO;
import edu.tamu.tcat.trc.entries.types.bio.dto.PersonNameDTO;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditPersonCommand;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;
import edu.tamu.tcat.trc.entries.types.bio.search.PeopleQueryCommand;
import edu.tamu.tcat.trc.entries.types.bio.search.PeopleSearchService;


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
   public RestApiV1.Person getPerson(@PathParam(value="personId") String personId) throws NoSuchCatalogRecordException
   {
      // FIXME make this a string based identifier
      // TODO make this a mangled string instead of an ID. Don't want people guessing
      //      unique identifiers
      // TODO add mappers for exceptions.
      //       CatalogRepoException should map to internal error.
      //       NoSuchCatalogRecordException should map to 404
      Person figure = repo.get(personId);
      return RepoAdapter.toDTO(figure);
   }
   
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.PersonId createPerson(RestApiV1.Person person) throws Exception
   {
      RestApiV1.PersonId personId = new RestApiV1.PersonId();
      EditPersonCommand createCommand = repo.create();

      createCommand.setAll(RepoAdapter.toRepo(person));
      String id = createCommand.execute().get();
      personId.id = id;
      return personId;
   }

   @PUT
   @Path("{personId}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.PersonId updatePerson(RestApiV1.Person person) throws Exception
   {
      EditPersonCommand updateCommand = repo.update(person.id);
      updateCommand.setAll(RepoAdapter.toRepo(person));
      updateCommand.execute().get();
      
      RestApiV1.PersonId personId = new RestApiV1.PersonId();
      personId.id = person.id;
      return personId;
   }

   @DELETE
   @Path("{personId}")
   @Consumes(MediaType.APPLICATION_JSON)
   public void deletePerson(@PathParam(value="personId") String personId) throws Exception
   {
      EditPersonCommand deleteCommand = repo.delete(personId);
      deleteCommand.execute();
   }

   /**
    * An encapsulation of adapter methods to convert between the repository API to the {@link RestApiV1}
    * schema DTOs.
    */
   private static class RepoAdapter
   {
      public static RestApiV1.Person toDTO(Person figure)
      {
         RestApiV1.Person dto = new RestApiV1.Person();
         dto.id = figure.getId();
   
         PersonName canonicalName = figure.getCanonicalName();
         if (canonicalName != null) {
            dto.displayName = toDTO(canonicalName);
         }
   
         dto.names = figure.getAlternativeNames().stream()
                        .map(RepoAdapter::toDTO)
                        .collect(Collectors.toSet());
   
         dto.birth = toDTO(figure.getBirth());
         dto.death = toDTO(figure.getDeath());
         dto.summary = figure.getSummary();
   
         return dto;
      }
      
      public static RestApiV1.PersonName toDTO(PersonName name)
      {
         RestApiV1.PersonName dto = new RestApiV1.PersonName();
   
         dto.title = name.getTitle();
         dto.givenName = name.getGivenName();
         dto.middleName = name.getMiddleName();
         dto.familyName = name.getFamilyName();
         dto.suffix = name.getSuffix();
   
         dto.displayName = name.getDisplayName();
   
         return dto;
      }
      
      public static RestApiV1.HistoricalEvent toDTO(HistoricalEvent orig)
      {
         RestApiV1.HistoricalEvent dto = new RestApiV1.HistoricalEvent();
         dto.id = orig.getId();
         dto.title = orig.getTitle();
         dto.description = orig.getDescription();
         dto.location = orig.getLocation();
         dto.date = toDTO(orig.getDate());
         return dto;
      }
      
      public static RestApiV1.DateDescription toDTO(DateDescription orig)
      {
         RestApiV1.DateDescription dto = new RestApiV1.DateDescription();
         LocalDate d = orig.getCalendar();
         if (d != null)
         {
            dto.calendar = DateTimeFormatter.ISO_LOCAL_DATE.format(d);
         }
   
         dto.description = orig.getDescription();
         
         return dto;
      }
      
      public static PersonDTO toRepo(RestApiV1.Person person)
      {
         PersonDTO dto = new PersonDTO();
         dto.id = person.id;
         if (person.displayName != null)
            dto.displayName = toRepo(person.displayName);
         
         dto.names = person.names.stream()
               .map(RepoAdapter::toRepo)
               .collect(Collectors.toSet());

         dto.birth = toRepo(person.birth);
         dto.death = toRepo(person.death);
         dto.summary = person.summary;
         return dto;
      }

      public static PersonNameDTO toRepo(RestApiV1.PersonName name)
      {
         PersonNameDTO dto = new PersonNameDTO();
   
         dto.title = name.title;
         dto.givenName = name.givenName;
         dto.middleName = name.middleName;
         dto.familyName = name.familyName;
         dto.suffix = name.suffix;
   
         dto.displayName = name.displayName;
   
         return dto;
      }
      
      public static HistoricalEventDV toRepo(RestApiV1.HistoricalEvent orig)
      {
         HistoricalEventDV dto = new HistoricalEventDV();
         dto.id = orig.id;
         dto.title = orig.title;
         dto.description = orig.description;
         dto.location = orig.location;
         dto.date = toRepo(orig.date);
         return dto;
      }
      
      public static DateDescriptionDV toRepo(RestApiV1.DateDescription orig)
      {
         DateDescriptionDV dto = new DateDescriptionDV();
         dto.calendar = orig.calendar;
         dto.description = orig.description;
         
         return dto;
      }
   }
}
