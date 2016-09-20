package edu.tamu.tcat.trc.services.types.bibref.rest.v1.internal;

import static java.text.MessageFormat.format;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * This utils class was copied from edu.tamu.tcat.trc.entries.types.bio.rest.v1.internal...
 * perhaps it should be made available in a shared TRC REST helper class
 */
public abstract class ApiUtils
{
   private final static Logger logger = Logger.getLogger(ApiUtils.class.getName());

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


   public static WebApplicationException handleExecutionException(String errMsg, ExecutionException e) throws Error
   {
      Throwable cause = e.getCause();
      if (Error.class.isInstance(cause))
         throw (Error)cause;

      return raise(Response.Status.INTERNAL_SERVER_ERROR, errMsg, Level.SEVERE, (Exception)e.getCause());
   }
}
