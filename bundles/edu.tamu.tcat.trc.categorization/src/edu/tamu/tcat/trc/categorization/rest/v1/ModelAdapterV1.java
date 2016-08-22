package edu.tamu.tcat.trc.categorization.rest.v1;

import static java.text.MessageFormat.format;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import edu.tamu.tcat.trc.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.categorization.rest.v1.CategorizationResource.TreeCategorizationResource;
import edu.tamu.tcat.trc.categorization.rest.v1.RestApiV1.BasicTreeNode;
import edu.tamu.tcat.trc.categorization.strategies.tree.TreeCategorization;
import edu.tamu.tcat.trc.categorization.strategies.tree.TreeNode;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;

public class ModelAdapterV1
{
   private final static Logger logger = Logger.getLogger(ModelAdapterV1.class.getName());

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

   public static RestApiV1.Categorization adapt(TreeCategorization scheme)
   {
      RestApiV1.Categorization dto = adaptBaseScheme(scheme);

      dto.type = TreeCategorizationResource.TYPE;
      dto.root = adapt(scheme.getRootNode());

      return dto;
   }

   private static RestApiV1.Categorization adaptBaseScheme(CategorizationScheme scheme)
   {
      RestApiV1.Categorization dto = new RestApiV1.Categorization();

      // TODO add additional metadata information
      dto.meta.id = scheme.getId();
      dto.key = scheme.getKey();
      dto.label = scheme.getLabel();
      dto.description = scheme.getDescription();

      return dto;
   }

   public static RestApiV1.BasicTreeNode adapt(TreeNode node)
   {
      BasicTreeNode dto = new RestApiV1.BasicTreeNode();

      dto.schemeId = node.getCategorization().getId();
      dto.id = node.getId();
      dto.label = node.getLabel();
      dto.description = node.getDescription();

      dto.entryRef = adapt(node.getAssociatedEntryRef());
      List<TreeNode> children = node.getChildren();
      dto.childIds = children.stream().map(TreeNode::getId).collect(Collectors.toList());
      dto.children = children.stream().map(ModelAdapterV1::adapt).collect(Collectors.toList());

      return dto;
   }

   private static RestApiV1.EntryReference adapt(EntryReference ref)
   {
      if (ref == null)
         return null;

      RestApiV1.EntryReference dto = new RestApiV1.EntryReference();
      dto.id = ref.id;
      dto.type = ref.type;
      dto.version = 0;

      return dto;
   }
}
