package edu.tamu.tcat.trc.services.rest.categorizations;

import static java.text.MessageFormat.format;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import edu.tamu.tcat.trc.services.ServiceContext;
import edu.tamu.tcat.trc.services.TrcServiceManager;
import edu.tamu.tcat.trc.services.categorization.CategorizationRepo;
import edu.tamu.tcat.trc.services.rest.categorizations.v1.CategorizationSchemesResource;

/**
 * Point of entry to the REST API for the Categorization system. This class is
 * designed to be configured and registered as an OSGi declarative service.
 *
 */
@Path("/")
public class CategorizationAPIService
{
   private final static Logger logger = Logger.getLogger(CategorizationAPIService.class.getName());


   private TrcServiceManager svcManager;

   public void bind(TrcServiceManager svcManager)
   {
      this.svcManager = svcManager;
   }

   public void activate()
   {
      try
      {
         logger.info(() -> "Activating " + getClass().getSimpleName());

         Objects.requireNonNull(svcManager, "service manager not available");
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "failed to activate categorizations REST API service.", e);
         throw e;
      }
   }

   @Path("/categorizations/{scope}")
   public CategorizationSchemesResource get(@PathParam("scope") String scopeId)
   {
      try
      {
         if (scopeId == null)
            throw new BadRequestException("No scope id provided");

         // TODO may adapt scope by translating username into account id
         if (scopeId.startsWith("@"))
            scopeId = adaptUsernameScope(scopeId);

         ServiceContext<CategorizationRepo> ctx = CategorizationRepo.makeContext(null, scopeId);
         CategorizationRepo repository = svcManager.getService(ctx);

         return new CategorizationSchemesResource(repository);
      }
      catch (Exception ex)
      {
         String pattern = "Failed to obtain categorization scheme for scope {0}";
         logger.log(Level.SEVERE, format(pattern, scopeId), ex);
         throw new InternalServerErrorException(ex);
      }
   }

   /**
    * Adapts scopes of the form /@username to use the user's account id as a scope.
    *
    * @param scopeId
    * @return
    */
   private String adaptUsernameScope(String scopeId)
   {
      // TODO Auto-generated method stub
      return null;
   }
}
