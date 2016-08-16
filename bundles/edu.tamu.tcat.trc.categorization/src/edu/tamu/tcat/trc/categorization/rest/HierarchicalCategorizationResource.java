package edu.tamu.tcat.trc.categorization.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.categorization.CategorizationRepo;
import edu.tamu.tcat.trc.categorization.CategorizationScheme;

public class HierarchicalCategorizationResource extends CategorizationResource
{

   /**
    * The type identifier for hierarchical categorizations.
    */
   public static final String TYPE = "hierarchical";

   public HierarchicalCategorizationResource(CategorizationRepo repo, CategorizationScheme scheme)
   {
      super(repo, scheme);
   }

   @Override
   @PUT     // TODO technically, this is a patch, not a put
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Categorization update(RestApiV1.Categorization updated)
   {
      // NOTE these will be ignored (meta will be updated
      updated.entries = null;
      throw new UnsupportedOperationException();
   }
}
