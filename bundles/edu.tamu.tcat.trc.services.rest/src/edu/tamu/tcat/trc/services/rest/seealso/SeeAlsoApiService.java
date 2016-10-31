package edu.tamu.tcat.trc.services.rest.seealso;

import static java.text.MessageFormat.format;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.services.rest.ApiUtils;
import edu.tamu.tcat.trc.services.rest.seealso.v1.SeeAlsoEntryResource;

@Path("/")
public class SeeAlsoApiService
{
   private final static Logger logger = Logger.getLogger(SeeAlsoApiService.class.getName());


   private TrcApplication trcFrameworkManager;

   public void bind(TrcApplication trc)
   {
      this.trcFrameworkManager = trc;
   }

   public void activate()
   {
      try
      {
         logger.info(() -> "Activating " + getClass().getSimpleName());

         Objects.requireNonNull(trcFrameworkManager, "framework manager not available");
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "failed to activate categorizations REST API service.", e);
         throw e;
      }
   }

   @Path("seealso/{token}")
   public SeeAlsoEntryResource getLinkResource(@PathParam("token") String token)
   {
      EntryId eId = null;
      try
      {
         eId = trcFrameworkManager.getResolverRegistry().decodeToken(token);
      }
      catch (Exception ex)
      {
         throw ApiUtils.raise(BAD_REQUEST, format("Invalid entry token {0}.", token), Level.INFO, null);
      }

      return new SeeAlsoEntryResource(eId, trcFrameworkManager);
   }
}
