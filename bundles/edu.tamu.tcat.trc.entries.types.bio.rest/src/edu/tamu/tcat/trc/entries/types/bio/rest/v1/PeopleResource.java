/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.entries.types.bio.rest.v1;

import static java.text.MessageFormat.format;

import java.net.URLEncoder;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.repo.DateDescriptionMutator;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditBiographicalEntryCommand;
import edu.tamu.tcat.trc.entries.types.bio.repo.HistoricalEventMutator;
import edu.tamu.tcat.trc.entries.types.bio.repo.PersonNameMutator;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.internal.ApiUtils;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.internal.RepoAdapter;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.internal.SearchAdapter;
import edu.tamu.tcat.trc.entries.types.bio.search.BioEntryQueryCommand;
import edu.tamu.tcat.trc.entries.types.bio.search.PersonSearchResult;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.QueryService;

public class PeopleResource
{
   // records internal errors accessing the REST
   static final Logger errorLogger = Logger.getLogger(PeopleResource.class.getName());

   private BiographicalEntryRepository repo;
   private QueryService<BioEntryQueryCommand> queryService;

   public PeopleResource(BiographicalEntryRepository repo, QueryService<BioEntryQueryCommand> queryService)
   {
      this.repo = repo;
      this.queryService = queryService;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
   public RestApiV1.PersonSearchResultSet
   searchPeople(@QueryParam(value="q") String q,
                @QueryParam(value = "off") @DefaultValue("0")   int offset,
                @QueryParam(value = "max") @DefaultValue("100") int numResults)
   {
      if (queryService == null)
      {
         Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                 .entity("Searching is not currently configured")
                 .type(MediaType.TEXT_XML)
                 .build();
         throw new WebApplicationException(response);
      }

      try
      {
         BioEntryQueryCommand cmd = queryService.createQuery();
         if (q != null)
            cmd.query(q);
         else
            cmd.queryAll();
         cmd.setOffset(offset);
         cmd.setMaxResults(numResults);
         PersonSearchResult results = cmd.executeSync();

         RestApiV1.PersonSearchResultSet rs = new RestApiV1.PersonSearchResultSet();
         rs.items = SearchAdapter.toDTO(results.get());

         buildQueryLinks(rs, q, offset, numResults);

         return rs;
      }
      catch (SearchException e)
      {
         errorLogger.log(Level.SEVERE, "Error", e);
         throw new InternalServerErrorException("Failed to execute search", e);
      }
   }

   private void buildQueryLinks(RestApiV1.PersonSearchResultSet rs, String q, int offset, int numResults)
   {
      //TODO: does next link depend on the number of results returned (i.e. whether < numResults), or do we assume there are infinite results?
      try
      {
         String linktemplate = "off={0}&max={1}&q={2}";
         String query = q != null ? URLEncoder.encode(q, "UTF-8") : "";

         rs.qs = MessageFormat.format(linktemplate, Integer.valueOf(offset), Integer.valueOf(numResults), query);
         rs.qsNext = MessageFormat.format(linktemplate, Integer.valueOf(offset + numResults), Integer.valueOf(numResults), query);
         if (offset >= numResults)
            rs.qsPrev = MessageFormat.format(linktemplate, Integer.valueOf(offset - numResults), Integer.valueOf(numResults), query);
         // first page got off; reset to zero offset
         else if (offset > 0 && offset < numResults)
            rs.qsPrev = MessageFormat.format(linktemplate, Integer.valueOf(0), Integer.valueOf(numResults), query);
      }
      catch (Exception e)
      {
         throw new InternalServerErrorException("Failed building querystring", e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Person createPerson(RestApiV1.Person person)
   {
      EditBiographicalEntryCommand command = repo.create();
      apply(command, person);

      errorLogger.log(Level.INFO, format("Creating new biographic entry for {0}", person.name.label));
      return execute(repo, command);
   }


   /**
    * Applied data from the REST DTO to the supplied command.
    *
    * @param command The command to apply the changes to
    * @param person The data to be updated for the associated person
    */
   static void apply(EditBiographicalEntryCommand command, RestApiV1.Person person)
   {
      applyName(command.editCanonicalName(), person.name);
      command.clearAlternateNames();
      for (RestApiV1.PersonName personName : person.altNames)
      {
         applyName(command.addAlternateName(), personName);
      }

      applyEvent(command.editBirth(), person.birth);
      applyEvent(command.editDeath(), person.death);

      command.setSummary(person.summary);
   }

   /**
    *
    * @param repo T
    * @param command
    *
    * @return A REST API representation of the data that was saved.
    *
    * @throws WebApplicationException For problems encountered during execution
    */
   static RestApiV1.Person execute(BiographicalEntryRepository repo, EditBiographicalEntryCommand command)
   {
      try
      {
         String id = command.execute().get(10, TimeUnit.SECONDS);

         // presumably we could just return the person that was supplied, but this ensures that we get
         // the result as stored locally.
         return RepoAdapter.toDTO(repo.get(id));
      }
      catch (InterruptedException | TimeoutException e)
      {
         String msg = "Oops. Things seem to be a bit busy now and we could not update the biographic entry in a timely manner. Please try again.";
         throw ApiUtils.raise(Response.Status.SERVICE_UNAVAILABLE, msg, Level.WARNING, e);
      }
      catch (ExecutionException ex)
      {
         throw handleExecutionException("Failed to create a new person", ex);
      }
   }

   static WebApplicationException handleExecutionException(String errMsg, ExecutionException e) throws Error
   {
      Throwable cause = e.getCause();
      if (Error.class.isInstance(cause))
         throw (Error)cause;

      return ApiUtils.raise(Response.Status.INTERNAL_SERVER_ERROR, errMsg, Level.SEVERE, (Exception)e.getCause());
   }


   private static void applyName(PersonNameMutator nameMutator, RestApiV1.PersonName name)
   {
      nameMutator.setDisplayName(name.label);
      nameMutator.setFamilyName(name.familyName);
      nameMutator.setGivenName(name.givenName);
      nameMutator.setMiddleName(name.middleName);
      nameMutator.setSuffix(name.suffix);
      nameMutator.setTitle(name.title);
   }

   @SuppressWarnings("deprecation")
   private static void applyEvent(HistoricalEventMutator mutator, RestApiV1.HistoricalEvent event)
   {
      mutator.setTitle(event.title);
      mutator.setDescription(event.description);
      mutator.setLocation(event.location);

      DateDescriptionMutator birthDateDescription = mutator.editDate();
      birthDateDescription.setDescription(event.date.description);
      birthDateDescription.setCalendar(adaptCalendarDate(event.date.calendar));
   }

   private static LocalDate adaptCalendarDate(String dateStr)
   {
      try {
         return (dateStr != null) ? LocalDate.parse(dateStr) : null;
      } catch (Exception ex) {
         errorLogger.log(Level.WARNING, format("Bad calendar date: {2}", dateStr), ex);
         return null;
      }
   }

   @Path("{personId}")
   public PersonResource getPerson(@PathParam(value="personId") String personId)
   {
      return new PersonResource(repo, personId);
   }
}
