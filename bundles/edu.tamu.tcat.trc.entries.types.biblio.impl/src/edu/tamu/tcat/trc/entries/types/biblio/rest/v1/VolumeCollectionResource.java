package edu.tamu.tcat.trc.entries.types.biblio.rest.v1;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.repo.VolumeMutator;
import edu.tamu.tcat.trc.entries.types.biblio.rest.EntityCollectionPersistenceAdapter;

public class VolumeCollectionResource
{

   private final EntityCollectionPersistenceAdapter<Volume, VolumeMutator> repoHelper;

   public VolumeCollectionResource(EntityCollectionPersistenceAdapter<Volume, VolumeMutator> helper)
   {
      this.repoHelper = helper;
   }

   /**
    * List all volumes on the current edition object
    *
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<RestApiV1.Volume> listVolumes()
   {
      return repoHelper.get().stream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());
   }

   /**
    * Create a new volume on the current edition object
    *
    * @param volume
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.VolumeId createVolume(RestApiV1.Volume volume)
   {
      RestApiV1.VolumeId vid = new RestApiV1.VolumeId();
      vid.id = repoHelper.create(mutator -> RepoAdapter.apply(volume, mutator));
      return vid;
   }

   /**
    * Fetch a specific volume from the edition and perform operations on it
    *
    * @param volumeId
    * @return
    */
   @Path("{id}")
   public VolumeResource getVolume(@PathParam("id") String volumeId)
   {
      return new VolumeResource(repoHelper.get(volumeId));
   }
}