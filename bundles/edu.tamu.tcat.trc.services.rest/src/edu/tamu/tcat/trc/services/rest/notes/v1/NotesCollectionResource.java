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
package edu.tamu.tcat.trc.services.rest.notes.v1;

import static java.text.MessageFormat.format;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.services.notes.EditNoteCommand;
import edu.tamu.tcat.trc.services.notes.Note;
import edu.tamu.tcat.trc.services.notes.NotesRepository;
import edu.tamu.tcat.trc.services.rest.ApiUtils;


public class NotesCollectionResource
{
   private final static Logger logger = Logger.getLogger(NotesCollectionResource.class.getName());

   private final NotesRepository repo;
   private final EntryResolverRegistry reg;

   private final ObjectMapper mapper;

   private final EntryReference ref;


   public NotesCollectionResource(NotesRepository repo,
                                  EntryResolverRegistry reg,
                                  EntryReference ref)
   {
      this.repo = repo;
      this.reg = reg;
      this.ref = null;

      // seems un-needed
      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   @GET
   @Path("{noteId}")
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Note get(String noteId)
   {
      try
      {
         Note note = repo.get(noteId).orElseThrow(() -> {
            String msg = format("Could not find note [id={0}].", noteId);
            return ApiUtils.raise(Response.Status.NOT_FOUND, msg, Level.INFO, null);
         });
         return ModelAdapter.adapt(reg, note);
      }
      catch (Exception e)
      {
         if (e instanceof WebApplicationException)
            throw e;

         String msg = format("Failed to retrieve note [id={0}].", noteId);
         throw ApiUtils.raise(Response.Status.INTERNAL_SERVER_ERROR, msg, Level.SEVERE, e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Note create(Map<String, Object> data)
   {
      // TODO need to assess and fix error handling.
      try
      {
         EditNoteCommand cmd = apply(repo.create(), data);
         Note note = cmd.exec().get(10, TimeUnit.SECONDS);
         return ModelAdapter.adapt(reg, note);
      }
      catch (Exception ex)
      {
         if (ex instanceof WebApplicationException)
            throw (WebApplicationException)ex;

         throw ApiUtils.raise(Response.Status.INTERNAL_SERVER_ERROR, "Failed to create note.", Level.SEVERE, unpackExecutionException(ex));
      }
   }

   @PUT
   @Path("{noteId}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Note update(@PathParam(value="noteId") String noteId, Map<String, Object> data)
   {
      try
      {
         EditNoteCommand cmd = apply(repo.edit(noteId), data);
         Note note = cmd.exec().get(10, TimeUnit.SECONDS);
         return ModelAdapter.adapt(reg, note);
      }
      catch (Exception ex)
      {
         if (ex instanceof WebApplicationException)
            throw (WebApplicationException)ex;

         throw ApiUtils.raise(Response.Status.INTERNAL_SERVER_ERROR, "Failed to create note.", Level.SEVERE, unpackExecutionException(ex));
      }
//      try
//      {
//         EditNoteCommand noteCommand = repo.edit(UUID.fromString(noteId));
//         noteCommand.setAll(noteDTO);
//
//         UUID id = noteCommand.execute().get();
//
//         RestApiV1.NotesId result = new RestApiV1.NotesId();
//         result.id = id.toString();
//         return result;
//      }
//      catch (DocumentNotFoundException noEx)
//      {
//         String msg = MessageFormat.format("Could not edit note [{0}]. No such note exists.", noteId);
//         logger.log(Level.WARNING, msg, noEx);
//         throw new NotFoundException(msg);
//      }
//      catch (Exception ie)
//      {
//         logger.log(Level.SEVERE, "Failed to update the supplied note.", ie);
//         throw new InternalServerErrorException("Failed to update the supplied note.");
//      }
   }

   @DELETE
   @Path("{noteid}")
   public void delete(@PathParam(value="noteid") String noteId)
   {
      // TODO send appropriate response.
      // repo.remove(UUID.fromString(noteId));
   }

   private Exception unpackExecutionException(Exception ex) throws Error
   {
      if (ex instanceof ExecutionException)
      {
         Throwable cause = ex.getCause();
         if (cause instanceof Error)
            throw (Error)cause;

         ex = (Exception)ex.getCause();
      }

      return ex;
   }

   private EditNoteCommand apply(EditNoteCommand cmd, Map<String, Object> data)
   {
      String mimeType = MediaType.TEXT_HTML;
      if (data.containsKey(RestApiV1.MIME_TYPE))
         mimeType = (String)data.get(RestApiV1.MIME_TYPE);
      cmd.setMimeType(mimeType);

      if (data.containsKey(RestApiV1.CONTENT))
      {
         String content = (String)data.get(RestApiV1.CONTENT);
         cmd.setContent(content);
      }

      // TODO pull account from parent.
      // TODO pull ref from parent.
      if (ref != null)
      {
         // TODO check to ensure that this matches supplied data
         cmd.setAssociatedEntry(ref);
      }

      return cmd;
   }
}
