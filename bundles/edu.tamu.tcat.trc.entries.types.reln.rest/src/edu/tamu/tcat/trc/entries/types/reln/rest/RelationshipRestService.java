package edu.tamu.tcat.trc.entries.types.reln.rest;

import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.types.reln.impl.search.RelnSearchStrategy;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.entries.types.reln.rest.v1.RelationshipTypeResource;
import edu.tamu.tcat.trc.entries.types.reln.rest.v1.RelationshipsResource;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipQueryCommand;
import edu.tamu.tcat.trc.search.solr.QueryService;

@Path("/")
public class RelationshipRestService
{
   private final static Logger logger = Logger.getLogger(RelationshipRestService.class.getName());

   private RelationshipTypeRegistry typeRegistry;
   private TrcApplication trcCtx;

   public void setTrcContext(TrcApplication trcCtx)
   {
      this.trcCtx = trcCtx;
   }

   public void setTypeRegistry(RelationshipTypeRegistry registry)
   {
      this.typeRegistry = registry;
   }

   /**
    * Lifecycle management callback (usually called by framework service layer)
    * This method should be called once all dependent services have been provided and the resource
    * is ready to start responding to requests.
    */
   public void activate()
   {
      logger.info("Activating " + getClass().getSimpleName());
   }

   @Path("v1/relationships")
   public RelationshipsResource getV1Endpoint()
   {
      RelnSearchStrategy indexCfg = new RelnSearchStrategy(trcCtx);
      QueryService<RelationshipQueryCommand> queryService = trcCtx.getQueryService(indexCfg);

      RelationshipRepository repo = trcCtx.getRepository(null, RelationshipRepository.class);
      return new RelationshipsResource(repo, queryService, trcCtx.getResolverRegistry());
   }

   @Path("relationships")
   public RelationshipsResource getDefaultEndpoint()
   {
      return getV1Endpoint();
   }

   @Path("/relationships/types")
   public RelationshipTypeResource getTypes()
   {
      return new RelationshipTypeResource(typeRegistry);
   }
}
