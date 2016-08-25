package edu.tamu.tcat.trc.categorization.rest.v1;

import static java.text.MessageFormat.format;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import edu.tamu.tcat.trc.categorization.CategorizationRepo;
import edu.tamu.tcat.trc.categorization.CategorizationScheme;

abstract class ApiUtils
{
   private final static Logger logger = Logger.getLogger(ApiUtils.class.getName());


   public static void checkUniqueKey(CategorizationRepo repo, String key)
   {
      // TODO may move into repo.
      if (key == null || key.trim().isEmpty())
         throw new BadRequestException("A key must be supplied for the categorization.");

      try
      {
         String errMsg = "The key [{0}] is already in use by scheme {1}.";
         CategorizationScheme scheme = repo.get(key);

         throw raise(Response.Status.CONFLICT, format(errMsg, key, scheme.getLabel()), null, null);
      }
      catch (IllegalArgumentException ex)
      {
         // no-op this is the expected behavior since the key should not be in use
      }
   }

   /**
    * Raises a {@link WebApplicationException} with the supplied status and error message.
    *
    * <p>
    * Removes some of the boiler plate for correctly sending clean error messages back to the
    * client. In general, the web framework (perhaps Jetty) we are using does a poor job of
    * translating {@link WebApplicationException} sub-types into useful error messages, so
    * we will supply our own. Additionally, the framework tends not to log errors, so this
    * method does that as well.
    *
    * @param status HTTP status level of the error
    * @param msg The error message
    * @param logLevel The log level or <code>null</code> to ignore logging.
    * @param e An exception to be logged. May be <code>null</code>.
    */
   public static WebApplicationException raise(Response.Status status, String msg, Level logLevel, Exception e)
   {
      if (logLevel != null)
      {
         UUID logId = UUID.randomUUID();
         String logMsg = format("{0} [Error Id: {1}]", msg, logId);
         if (e != null)
            logger.log(logLevel, logMsg, e);
         else
            logger.log(logLevel, logMsg);

         String template = "{0}\n\nDetails of this message have been recorded. Please reference the following error id: {1}";
         msg = format(template, msg, logId);
      }

      ResponseBuilder builder = Response
            .status(status)
            .type(MediaType.TEXT_PLAIN + ";charset=UTF-8")
            .entity(msg);
      return new WebApplicationException(builder.build());
   }
}
