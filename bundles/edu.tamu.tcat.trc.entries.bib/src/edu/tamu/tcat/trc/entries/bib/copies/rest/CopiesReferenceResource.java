package edu.tamu.tcat.trc.entries.bib.copies.rest;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.bib.UpdateCanceledException;
import edu.tamu.tcat.trc.entries.bib.WorkRepository;
import edu.tamu.tcat.trc.entries.bib.copies.CopyReference;
import edu.tamu.tcat.trc.entries.bib.copies.CopyReferenceRepository;
import edu.tamu.tcat.trc.entries.bib.copies.EditCopyReferenceCommand;
import edu.tamu.tcat.trc.entries.bib.copies.model.CopyRefDTO;

@Path("/copies")
public class CopiesReferenceResource
{
   private WorkRepository repo;
   private CopyReferenceRepository copiesRepo;
   private ObjectMapper mapper;

   // Called by DS
   public void setRepository(WorkRepository repo)
   {
      this.repo = repo;
   }

   public void setCopyRepository(CopyReferenceRepository repo)
   {
      copiesRepo = repo;
   }

   // called by DS
   public void activate()
   {
      Objects.requireNonNull(repo, "No bibliographic work repository configured");
      Objects.requireNonNull(copiesRepo, "No copy reference repository configured");

      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   // called by DS
   public void dispose()
   {
      repo = null;
      copiesRepo = null;
      mapper = null;
   }

   @GET
   @Path("{entityId : works/.+}")
   @Produces(MediaType.APPLICATION_JSON)
   public List<CopyRefDTO> getByWorkId(@PathParam(value = "entityId") String entityId)
   {
      // FIXME requires error handling
      URI uri = URI.create(entityId);
      List<CopyReference> matchedCopies = copiesRepo.getCopies(uri);
      return matchedCopies.parallelStream()
                          .map(CopyRefDTO::create)
                          .collect(Collectors.toList());
   }

   @GET
   @Path("{refId : [0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}}")
   @Produces(MediaType.APPLICATION_JSON)
   public CopyRefDTO getByRefId(@PathParam(value = "refId") String refId)
   {
      // TODO requires better error handling
      UUID id = UUID.fromString(refId);
      try
      {
         CopyReference reference = copiesRepo.get(id);
         return CopyRefDTO.create(reference);
      }
      catch (NoSuchCatalogRecordException e)
      {
         throw new NotFoundException("Could not find copy [" + refId +"]");
      }
   }

   /**
    * Updates an existing copy reference.
    *
    * @param refId
    * @return
    */
   @PUT
   @Path("{refId : [0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public Response updateRef(@PathParam(value = "refId") String refId, CopyRefDTO dto)
   {
      // FIXME fix error handling!!
      try
      {
         UUID id = UUID.fromString(refId);
         if (dto.id != null && !dto.id.equals(id))
            throw new BadRequestException("Copy reference id [" + dto.id + "] does not match resource id [" + id + "]");
         else if (dto.id == null)
            dto.id = id;

         // TODO check to see if this reference exists - if not, create it
         EditCopyReferenceCommand command = copiesRepo.edit(id);
         command.update(dto);

         CopyReference ref = command.execute().get(10, TimeUnit.SECONDS);

         // things that could go wrong. general Exception, timeout, illegal arg updating command
         String json = mapper.writeValueAsString(CopyRefDTO.create(ref));
         ResponseBuilder builder = Response.ok(json, MediaType.APPLICATION_JSON);

         return builder.build();
      }
      catch (NoSuchCatalogRecordException arg)
      {
         throw new NotFoundException("Invalid reference id [" + refId + "]");
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to update reference [" + refId + "].", e);
      }
   }

   /**
    * Add a new copy reference
    *
    * @param entityId
    * @return
    * @throws UpdateCanceledException
    */
   @POST
   @Path("{entityId : works/.+}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public Response createByWorkId(@PathParam(value = "entityId") String entityId, CopyRefDTO dto) throws UpdateCanceledException
   {
      // FIXME fix error handling!!
      // TODO response should supply URL of resource
      try
      {
         URI entityUri = URI.create(entityId);
         if (!entityUri.equals(dto.associatedEntry))
            throw new BadRequestException("Copy reference id [" + dto.associatedEntry + "] does not match resource id [" + entityId + "]");

         if (dto.id == null)
            dto.id = UUID.randomUUID();

         // TODO verify valid copy id

         EditCopyReferenceCommand command = copiesRepo.create();
         command.update(dto);

         CopyReference ref = command.execute().get(10, TimeUnit.SECONDS);

         // things that could go wrong. general Exception, timeout, illegal arg updating command
         String json = mapper.writeValueAsString(CopyRefDTO.create(ref));
         ResponseBuilder builder = Response.ok(json, MediaType.APPLICATION_JSON);

         return builder.build();
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to update reference [" + entityId + "].", e);
      }
   }


}
