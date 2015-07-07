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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;
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

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditPersonCommand;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;
import edu.tamu.tcat.trc.entries.types.bio.search.PeopleQueryCommand;
import edu.tamu.tcat.trc.entries.types.bio.search.PeopleSearchService;
import edu.tamu.tcat.trc.entries.types.bio.search.PersonSearchResult;
import edu.tamu.tcat.trc.search.SearchException;


@Path("/people")
public class PeopleResource
{
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
//   public RestApiV1.PersonSearchResultSet
   public List<RestApiV1.PersonSearchResult>
   searchPeople(@QueryParam(value="syntheticName") String q,
                @QueryParam(value = "off") @DefaultValue("0")   int offset,
                @QueryParam(value = "max") @DefaultValue("100") int numResults)
   throws Exception
   {
      try
      {
         PeopleQueryCommand cmd = peopleSearchService.createQueryCommand();
         if (q != null)
            cmd.query(q);
         cmd.setOffset(offset);
         cmd.setMaxResults(numResults);
         PersonSearchResult results = cmd.execute();

         RestApiV1.PersonSearchResultSet rs = new RestApiV1.PersonSearchResultSet();
         rs.items = SearchAdapter.toDTO(results.get());

         StringBuilder sb = new StringBuilder();
         try
         {
            app(sb, "q", q);
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
         else if (offset > 0 && offset < numResults)
            rs.qsPrev = "off="+(0)+"&max="+numResults+"&"+sb.toString();

         //HACK: until the JS is ready to accept this data vehicle, just send the list of results
//         return rs;
         return rs.items;
      }
      catch (Exception e)
      {
         errorLogger.log(Level.SEVERE, "Error", e);
         throw new SearchException(e);
      }
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
}
