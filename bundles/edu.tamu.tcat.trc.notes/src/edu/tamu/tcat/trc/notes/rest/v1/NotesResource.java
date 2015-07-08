package edu.tamu.tcat.trc.notes.rest.v1;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
import edu.tamu.tcat.trc.notes.Note;
import edu.tamu.tcat.trc.notes.dto.NotesDTO;
import edu.tamu.tcat.trc.notes.repo.EditNotesCommand;
import edu.tamu.tcat.trc.notes.repo.NotesRepository;


@Path("/notes")
public class NotesResource
{

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
   public RestApiV1.NotesId create(NotesDTO noteDTO) throws InterruptedException, ExecutionException
   {
      EditNotesCommand noteCommand = repo.create();
      noteCommand.setAll(noteDTO);
      RestApiV1.NotesId noteId = new RestApiV1.NotesId();
      Note notes = noteCommand.execute().get();
      noteId.id = notes.getId().toString();
      return noteId;
   }

   @PUT
   @Path("{noteid}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.NotesId update(@PathParam(value="noteid") String noteId, NotesDTO noteDTO) throws InterruptedException, ExecutionException, NoSuchCatalogRecordException
   {
      EditNotesCommand noteCommand = repo.edit(UUID.fromString(noteId));
      noteCommand.setAll(noteDTO);
      RestApiV1.NotesId restNoteId = new RestApiV1.NotesId();
      Note notes = noteCommand.execute().get();
      restNoteId.id = notes.getId().toString();
      return restNoteId;
   }

   @DELETE
   @Path("{noteid}")
   public void delete(@PathParam(value="noteid") String noteId)
   {
      repo.remove(UUID.fromString(noteId));
   }
}
