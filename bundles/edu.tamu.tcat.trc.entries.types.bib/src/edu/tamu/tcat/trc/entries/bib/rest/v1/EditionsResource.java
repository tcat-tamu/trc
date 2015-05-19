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
import edu.tamu.tcat.trc.entries.bib.Work;
import edu.tamu.tcat.trc.entries.bib.WorkRepository;
import edu.tamu.tcat.trc.entries.bib.dto.CustomResultsDV;
import edu.tamu.tcat.trc.entries.bib.dto.EditionDV;

@Path("/works/{workId}/editions")
public class EditionsResource
{
   private WorkRepository repo;


   // Called by DS
   public void setRepository(WorkRepository repo)
   {
      this.repo = repo;
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
   @Produces(MediaType.APPLICATION_JSON)
   public Collection<EditionDV> listEditions(@PathParam(value = "workId") String workId) throws NumberFormatException, NoSuchCatalogRecordException
   {
      Work work = repo.getWork(workId);

      Collection<Edition> editions = work.getEditions();

      return editions.parallelStream()
            .map(EditionDV::create)
            .collect(Collectors.toSet());
   }

   @GET
   @Path("{editionId}")
   @Produces(MediaType.APPLICATION_JSON)
   public EditionDV getEdition(@PathParam(value = "workId") String workId,
                                 @PathParam(value = "editionId") String editionId) throws NumberFormatException, NoSuchCatalogRecordException
   {
      Edition edition = repo.getEdition(workId, editionId);
      return EditionDV.create(edition);
   }

   @PUT
   @Path("{editionId}")
   @Produces(MediaType.APPLICATION_JSON)
   public CustomResultsDV updateEdition(@PathParam(value = "workId") String workId,
                                        @PathParam(value = "editionId") String editionId,
                                        EditionDV edition)
                                              throws NoSuchCatalogRecordException
   {
      EditWorkCommand command = repo.edit(workId);
      EditionMutator editionMutator = command.editEdition(editionId);
      editionMutator.setAll(edition);
      command.execute();
      return new CustomResultsDV(editionMutator.getId());
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public CustomResultsDV createEdition(@PathParam(value = "workId") String workId, EditionDV edition) throws NoSuchCatalogRecordException
   {
      EditWorkCommand command = repo.edit(workId);
      EditionMutator editionMutator = command.createEdition();
      editionMutator.setAll(edition);
      command.execute();
      return new CustomResultsDV(editionMutator.getId());
   }

   @DELETE
   @Path("{editionId}")
   public void deleteWork(@PathParam(value = "workId") String workId,
                          @PathParam(value = "editionId") String editionId) throws NoSuchCatalogRecordException
   {
      EditWorkCommand command = repo.edit(workId);
      command.removeEdition(editionId);
      command.execute();
   }
}