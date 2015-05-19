package edu.tamu.tcat.trc.entries.bio.rest.v1;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;

/**
 *
 * @param <T> Must be JSON serializable.
 */
public class ErrorResponseData<T>
{
   public static final String PROP_ENABLE_ERR_DETAILS = "rest.err.details.enabled";

   public T resource;
   public Response.Status status;
   public String message;
   public String details;

   public ErrorResponseData()
   {

   }

   ErrorResponseData(T resource, Response.Status status, String message, String detail)
   {
      this.resource = resource;
      this.status = status;
      this.message = message;
      this.details = detail != null ? detail : message;
   }

   /**
    *
    * @param error
    * @return
    */
   public static <X> Response createJsonResponse(ErrorResponseData<X> error)
   {
      Response resp = Response.status(error.status)
                              .entity(error)
                              .type(MediaType.APPLICATION_JSON)
                              .build();
      return resp;
   }

   public static String getErrorDetail(Exception ex, ConfigurationProperties properties)
   {
      Boolean enableDetails = properties.getPropertyValue(PROP_ENABLE_ERR_DETAILS, Boolean.class, Boolean.valueOf(false));

      if (enableDetails.booleanValue())
         return null;

      try (StringWriter sw = new StringWriter();
           PrintWriter writer = new PrintWriter(sw))
      {
         ex.printStackTrace(writer);
         return sw.toString();
      }
      catch (Exception e) {
         String msg = "Failed to generate exception details : " + e.getMessage();
         PeopleResource.errorLogger.log(Level.SEVERE, msg, e);
         return msg;
      }
   }
}