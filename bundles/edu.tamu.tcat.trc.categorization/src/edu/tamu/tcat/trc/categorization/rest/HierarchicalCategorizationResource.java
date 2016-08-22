package edu.tamu.tcat.trc.categorization.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.trc.categorization.CategorizationRepo;
import edu.tamu.tcat.trc.categorization.rest.CategorizationNodeResource.TreeNodeResource;
import edu.tamu.tcat.trc.categorization.strategies.tree.TreeCategorization;

public class HierarchicalCategorizationResource extends CategorizationResource
{

   /**
    * The type identifier for hierarchical categorizations.
    */
   public static final String TYPE = "hierarchical";

   public HierarchicalCategorizationResource(CategorizationRepo repo, TreeCategorization scheme)
   {
      super(repo, scheme);
   }

   /**
    * Moves nodes from one position within the entry structure to another.
    *
    * This method is not supported for SetCategorizations. For ListCategorizations,
    * information about the parent entry will be ignored if present.
    *
    * @param updates A list of entry movements to be applied in order.
    * @return
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public Response moveEntries(List<RestApiV1.MoveEntry> updates)
   {
      return null;
   }

   @Override
   public CategorizationNodeResource<?> getNode(@PathParam("id") String nodeId)
   {
      return new TreeNodeResource(repo, (TreeCategorization)scheme, nodeId);
   }
}
