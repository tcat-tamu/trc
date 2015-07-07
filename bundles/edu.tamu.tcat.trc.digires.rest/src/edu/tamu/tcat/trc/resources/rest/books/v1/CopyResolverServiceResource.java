package edu.tamu.tcat.trc.resources.rest.books.v1;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.resources.books.resolve.CopyResolverRegistry;
import edu.tamu.tcat.trc.resources.books.resolve.CopyResolverStrategy;
import edu.tamu.tcat.trc.resources.books.resolve.DigitalCopy;
import edu.tamu.tcat.trc.resources.books.resolve.ResourceAccessException;
import edu.tamu.tcat.trc.resources.books.resolve.UnsupportedCopyTypeException;

@Path("/resources/books")
public class CopyResolverServiceResource
{
   // TODO split into logical services
   private static final Logger logger = Logger.getLogger(CopyResolverServiceResource.class.getName());

   private CopyResolverRegistry copyResolverReg;

   public void setResolverRegistry(CopyResolverRegistry registry)
   {
      // TODO make dynamic
      this.copyResolverReg = registry;
   }


   public void activate()
   {
   }

   public void dispose()
   {
   }

   /**
    * Retrieves information about a HathiFile record given an identifier.
    *
    * NOTE: path parameters do not work with the '#' character in the identifier,
    *       so we are falling back on a URL-encoded query parameter for the moment
    * @param id
    * @return
    * @throws ResourceAccessException
    * @throws IllegalArgumentException
    */
   @GET
//   @Path("{identifier}")
   @Produces(MediaType.APPLICATION_JSON)
   public DigitalCopy retrieve(@QueryParam(value = "identifier") String id) throws ResourceAccessException, IllegalArgumentException
   {
      CopyResolverStrategy<?> strategy;
      try
      {
         strategy = copyResolverReg.getResolver(id);
      }
      catch (UnsupportedCopyTypeException e)
      {
         throw new IllegalArgumentException("Could not retrieve the digital copy [" + id + "]. No copy resolver has been registered that recognizes this type of copy.", e);
      }

      return strategy.resolve(id);
   }



}
