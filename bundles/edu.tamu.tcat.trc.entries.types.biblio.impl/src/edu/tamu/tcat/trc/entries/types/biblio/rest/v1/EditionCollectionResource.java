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

import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.entries.types.biblio.rest.EntityCollectionPersistenceAdapter;

public class EditionCollectionResource
{
   private final EntityCollectionPersistenceAdapter<Edition, EditionMutator> collectionPersistenceAdapter;

   public EditionCollectionResource(EntityCollectionPersistenceAdapter<Edition, EditionMutator> collectionPersistenceAdapter)
   {
      this.collectionPersistenceAdapter = collectionPersistenceAdapter;
   }

   /**
    * List all editions on the current work object
    *
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<RestApiV1.Edition> listEditions()
   {
      return collectionPersistenceAdapter.get().stream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());
   }

   /**
    * Create a new edition on the current work object
    *
    * @param edition
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Edition createEdition(RestApiV1.Edition edition)
   {
      String id = collectionPersistenceAdapter.create(mutator -> RepoAdapter.apply(edition, mutator));
      Edition createdEdition = collectionPersistenceAdapter.get(id).get();
      return RepoAdapter.toDTO(createdEdition);
   }

   /**
    * Fetch a specific edition from the work and perform operations on it
    *
    * @param editionId
    * @return
    */
   @Path("{id}")
   public EditionResource getEdition(@PathParam("id") String editionId)
   {
      return new EditionResource(collectionPersistenceAdapter.get(editionId));
   }
}