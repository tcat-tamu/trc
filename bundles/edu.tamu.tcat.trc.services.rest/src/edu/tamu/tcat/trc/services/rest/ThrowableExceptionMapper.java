package edu.tamu.tcat.trc.services.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

//Adapted from http://stackoverflow.com/a/19680115
/**
 * This will only be used if the Throwable is not a WebApplicationException with an entity. If it has an entity,
 * it seems to be handled by the framework before looking for mappers.
 *
 * This mapper allows code of the form:
 * <code>
 * throw new BadRequestException("status message");
 * </code>
 *
 * to be treated as:
 * <code>
 * throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).entity("status message").build());
 * </code>
 * Which means the exception message is sent as the entity of the response instead of ignored in the response.
 */
@Provider
public class ThrowableExceptionMapper implements ExceptionMapper<Throwable>
{
   private static final Logger debug = Logger.getLogger(ThrowableExceptionMapper.class.getName());
   @Context
   HttpServletRequest request;

   @Override
   public Response toResponse(Throwable exception)
   {
      if (exception instanceof WebApplicationException)
      {
         debug.log(Level.FINE, "WebAppException", exception);
         Response orig = ((WebApplicationException)exception).getResponse();
         String msg = exception.getMessage();
         if (orig.hasEntity() || msg == null)
            return orig;
         // If the original
         return Response.fromResponse(orig).type(MediaType.TEXT_PLAIN).entity(msg).build();
      }

      String errorMessage = buildErrorMessage(request);
      debug.log(Level.WARNING, errorMessage, exception);
      return Response.serverError()
                      // send text-plain instead of a previously defined type, such as
                      // JSON, which will fail parsing on the client
                     .type(MediaType.TEXT_PLAIN)
                     .entity("Error has been logged.  Inform support of issue.")
                     .build();
   }

   private String buildErrorMessage(HttpServletRequest req)
   {
      StringBuilder message = new StringBuilder();
//      String entity = "(empty)";
//
//      // How to cache getInputStream: http://stackoverflow.com/a/17129256/356408
//      try (InputStream is = req.getInputStream();
//           Scanner s = new Scanner(is, "UTF-8"))
//      {
//         // Read an InputStream elegantly: http://stackoverflow.com/a/5445161/356408
//         s.useDelimiter("\\A");
//         entity = s.hasNext() ? s.next() : entity;
//      }
//      catch (Exception ex)
//      {
//         // Ignore exceptions around getting the entity
//      }

      message.append("Uncaught REST API exception:\n");
      message.append("URL: ").append(getOriginalURL(req)).append("\n");
      message.append("Method: ").append(req.getMethod()).append("\n");
//      message.append("Entity: ").append(entity).append("\n");

      return message.toString();
   }

   private String getOriginalURL(HttpServletRequest req)
   {
      // Rebuild the original request URL: http://stackoverflow.com/a/5212336/356408
      String scheme = req.getScheme(); // http
      String serverName = req.getServerName(); // hostname.com
      int serverPort = req.getServerPort(); // 80
      String contextPath = req.getContextPath(); // /mywebapp
      String servletPath = req.getServletPath(); // /servlet/MyServlet
      String pathInfo = req.getPathInfo(); // /a/b;c=123
      String queryString = req.getQueryString(); // d=789

      // Reconstruct original requesting URL
      StringBuilder url = new StringBuilder();
      url.append(scheme).append("://").append(serverName);

      if (serverPort != 80 && serverPort != 443)
      {
         url.append(":").append(serverPort);
      }

      url.append(contextPath).append(servletPath);

      if (pathInfo != null)
      {
         url.append(pathInfo);
      }

      if (queryString != null)
      {
         url.append("?").append(queryString);
      }

      return url.toString();
   }
}
