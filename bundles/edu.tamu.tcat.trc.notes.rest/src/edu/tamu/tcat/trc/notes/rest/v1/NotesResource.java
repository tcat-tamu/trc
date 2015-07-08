package edu.tamu.tcat.trc.notes.rest.v1;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.notes.dto.NoteDTO;
import edu.tamu.tcat.trc.notes.repo.EditNoteCommand;
import edu.tamu.tcat.trc.notes.repo.NotesRepository;


@Path("/notes")
public class NotesResource
{
   private final static Logger logger = Logger.getLogger(NotesResource.class.getName());

   private NotesRepository repo;
   private ObjectMapper mapper;

   public void setRepository(NotesRepository repo)
   {
      this.repo = repo;

   }

   public void activate()
   {
      Objects.requireNonNull(repo, "Notes Repsoitory was not setup correctly.");
      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   public void dispose()
   {
      repo = null;
      mapper = null;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<RestApiV1.NotesSearchResult> search(@QueryParam(value="q") String q)
   {
      return null;
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.NotesId create(NoteDTO noteDTO)
   {
      // TODO need to asses and fix error handling.
      try
      {
         EditNoteCommand noteCommand = repo.create();
         noteCommand.setAll(noteDTO);

         UUID id = noteCommand.execute().get();

         RestApiV1.NotesId noteId = new RestApiV1.NotesId();
         noteId.id = id.toString();
         return noteId;
      }
      catch (ExecutionException ex)
      {
         logger.log(Level.SEVERE, "Failed to update the supplied note.", ex);
         throw new InternalServerErrorException("Failed to update the supplied note.");
      }
      catch (Exception ie)
      {
         logger.log(Level.SEVERE, "Failed to update the supplied note.", ie);
         throw new InternalServerErrorException("Failed to update the supplied note.");
      }
   }

   @PUT
   @Path("{noteid}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.NotesId update(@PathParam(value="noteid") String noteId, NoteDTO noteDTO) throws InterruptedException, ExecutionException, NoSuchCatalogRecordException
   {
      try
      {
         EditNoteCommand noteCommand = repo.edit(UUID.fromString(noteId));
         noteCommand.setAll(noteDTO);

         UUID id = noteCommand.execute().get();

         RestApiV1.NotesId result = new RestApiV1.NotesId();
         result.id = id.toString();
         return result;
      }
      catch (NoSuchCatalogRecordException noEx)
      {
         String msg = MessageFormat.format("Could not edit note [{0}]. No such note exists.", noteId);
         logger.log(Level.WARNING, msg, noEx);
         throw new NotFoundException(msg);
      }
      catch (Exception ie)
      {
         logger.log(Level.SEVERE, "Failed to update the supplied note.", ie);
         throw new InternalServerErrorException("Failed to update the supplied note.");
      }
   }

   @DELETE
   @Path("{noteid}")
   public void delete(@PathParam(value="noteid") String noteId)
   {
      // TODO send appropriate response.
      repo.remove(UUID.fromString(noteId));
   }
}
