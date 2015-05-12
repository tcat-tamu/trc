package edu.tamu.tcat.trc.entries.bib.rest.v1;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.bib.EditWorkCommand;
import edu.tamu.tcat.trc.entries.bib.Edition;
import edu.tamu.tcat.trc.entries.bib.EditionMutator;
import edu.tamu.tcat.trc.entries.bib.Volume;
import edu.tamu.tcat.trc.entries.bib.VolumeMutator;
import edu.tamu.tcat.trc.entries.bib.WorkRepository;
import edu.tamu.tcat.trc.entries.bib.dto.CustomResultsDV;
import edu.tamu.tcat.trc.entries.bib.dto.VolumeDV;

@Path("/works/{workId}/editions/{editionId}/volumes")
public class VolumesResource
{
   private WorkRepository repo;

   public void activate()
   {
   }

   public void dispose()
   {
   }

   public void setRepository(WorkRepository repo)
   {
      this.repo = repo;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Collection<VolumeDV> listVolumes(@PathParam(value = "workId") String workId,
                                           @PathParam(value = "editionId") String editionId) throws NumberFormatException, NoSuchCatalogRecordException
   {
      Edition edition = repo.getEdition(workId, editionId);
      return edition.getVolumes().stream()
            .map(VolumeDV::create)
            .collect(Collectors.toSet());
   }

   @GET
   @Path("{volumeId}")
   @Produces(MediaType.APPLICATION_JSON)
   public VolumeDV getVolume(@PathParam(value = "workId") String workId,
                             @PathParam(value = "editionId") String editionId,
                             @PathParam(value = "volumeId") String volumeId) throws NumberFormatException, NoSuchCatalogRecordException
   {
      Volume volume = repo.getVolume(workId, editionId, volumeId);
      return VolumeDV.create(volume);
   }

   @PUT
   @Path("{volumeId}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public CustomResultsDV updateVolume(@PathParam(value = "workId") String workId,
                                       @PathParam(value = "editionId") String editionId,
                                       @PathParam(value = "volumeId") String volumeId,
                                       VolumeDV volume) throws NoSuchCatalogRecordException
   {
      EditWorkCommand editWorkCommand = repo.edit(workId);
      EditionMutator editionMutator = editWorkCommand.editEdition(editionId);
      VolumeMutator volumeMutator = editionMutator.editVolume(volumeId);
      volumeMutator.setAll(volume);
      editWorkCommand.execute();
      return new CustomResultsDV(volumeMutator.getId());
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public CustomResultsDV createVolume(@PathParam(value = "workId") String workId,
                                       @PathParam(value = "editionId") String editionId,
                                       VolumeDV volume) throws NoSuchCatalogRecordException
   {
      EditWorkCommand editWorkCommand = repo.edit(workId);
      EditionMutator editionMutator = editWorkCommand.editEdition(editionId);
      VolumeMutator volumeMutator = editionMutator.createVolume();
      volumeMutator.setAll(volume);
      editWorkCommand.execute();
      return new CustomResultsDV(volumeMutator.getId());
   }

   @DELETE
   @Path("{volumeId}")
   public void deleteWork(@PathParam(value = "workId") String workId,
                          @PathParam(value = "volumeId") String volumeId) throws NoSuchCatalogRecordException
   {
      EditWorkCommand command = repo.edit(workId);
      command.removeVolume(volumeId);
      command.execute();
   }
}
