package edu.tamu.tcat.trc.categorization.rest.v1;

import static java.text.MessageFormat.format;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.trc.categorization.CategorizationRepo;
import edu.tamu.tcat.trc.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.categorization.EditCategorizationCommand;
import edu.tamu.tcat.trc.categorization.rest.v1.CategorizationResource.TreeCategorizationResource;
import edu.tamu.tcat.trc.categorization.strategies.tree.TreeCategorization;

/**
 *  Represents categorization schemes. A categorization is a user-defined structure designed
 *  to organize or order a particular sub-set of resources within a thematic research
 *  collection (TRC). For example, a categorization could be used to define a set of key
 *  bibliographic entries for readers new to a particular topic to start reading,
 *  a hierarchy of articles that provide encyclopedic coverage of a particular domain,
 *  or a set of people that a particular user wants to learn more about.
 *
 *  <p>
 *  Categorizations are designed to give application developers, collection administrators
 *  and individual users tools to provide additional structure over the material within
 *  the TRC. While there are numerous candidate applications for categorizations, key
 *  use cases include:
 *
 *  <ul>
 *    <li>defined content areas within the project website</li>
 *    <li>curated exhibits for public engagement</li>
 *    <li>personal notebooks and collections</li>
 *    <li>modules for teaching and self-study</li>
 *  </ul>
 *
 *  <h2>Categorization Strategies</h2>
 *  <p>Since there are many different ways to structure and organize information,
 *  categorizations support a few well-defined strategies for structuring content.
 *  Each categorization provides the same basic set of descriptive information
 *  and contains entries that provide the internal structure of the categorization.
 *  Current categorization strategies include hierarchies, ordered lists and
 *  unordered sets. We envision adding additional strategies as the need arises.
 *
 *  <h2>Categorization Scopes</h2>
 *  <p>Categorizations are defined relative to some scope. For example, some may be
 *  global to the site and integral to the vision of the TRC, such as an encyclopedia
 *  of reference articles. Other categorizations might reflect personal note-taking
 *  tools for users such as notebooks. Still others might reflect group efforts such
 *  as a set of people compiled as part of a group project for a class.
 *
 *  <p>To support this, the {@link CategorizationSchemesResource} is designed to be
 *  configured as a sub-resource and provided within a particular scoped context
 *  such as a user's personal categorizations, the categorizations maintained
 *  globally by the TRC editors, etc. The specific API endpoint that provides
 *  access to a {@code CategorizationSchemesResource} should document the nature and
 *  purpose of categorization within that scope, but will provide a common API
 *  implementation.
 *
 *  <h2>Workplan Notes</h2>
 *  <p><b>This is currently work in progress. We will start by implementing this
 *  support for articles and hierarchical organizations and extend it in the near
 *  future to incorporate other features.
 *
 */
public class CategorizationSchemesResource
{
   private final static Logger logger = Logger.getLogger(CategorizationSchemesResource.class.getName());

   private final CategorizationRepo repo;

   public CategorizationSchemesResource(CategorizationRepo repo)
   {
      this.repo = repo;
   }

   // TODO need a 'list' endpoint

   /**
    *  Create a new categorization.
    *
    *  @param key The identifier for this categorization.
    *  @return A created response with a link to the created categorization. The created
    *       categorization will be supplied in the body.
    */
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   public RestApiV1.Categorization createCategorization(RestApiV1.CategorizationDesc categorization)
   {
      String logEntryMsg = "Attempting to create new categorization scheme {0}/{1} ({2}).";
      logger.fine(() -> format(logEntryMsg, repo.getScope().getScopeId(), categorization.key, categorization.label));

      try
      {
         ApiUtils.checkUniqueKey(repo, categorization.key);

         String errBadType = "Cannot create new categorization. Categorizatons of the requested type {0} are not supported.";
         switch (categorization.type)
         {
            // FIXME add support for other types
            case TreeCategorizationResource.TYPE:
               return createTreeCategorization(categorization);
            default:
               throw raise(Response.Status.BAD_REQUEST, format(errBadType, categorization.type));
         }

      }
      catch (WebApplicationException ex)
      {
         String logMsg = "Error attempting to create a categorization scheme: {0}/{1} ({2}).";
         String msg = format(logMsg, repo.getScope().getScopeId(), categorization.key, categorization.label);
         logger.log(Level.INFO, msg, ex);
         throw ex;
      }
   }

   /**
    * Looks up a categorization by its scope specific scheme.
    *
    * <p>Note that, while keys are expected to remain relatively stable and are
    * suitable for general use to improve readability, they may change over time
    * and are not suitable in contexts that require persistent links. Where persistent
    * links are required, see {@link #getCategorizationById(String)}, which uses the
    * application defined id.
    *
    * @param key The key for the categorization scheme to return. Keys are
    *       semi-readable identifiers that must be unique within a given
    *       categorization scope.
    * @return The identified categorization scheme.
    *
    * @throws NotFoundException If no categorization is defined for the provided key.
    */
   @Path("{key}")
   public CategorizationResource getCategorization(@PathParam("key") String key)
   {
      Optional<CategorizationScheme> opt = getByKey(key);
      if (!opt.isPresent())
         opt = getById(key);

      String notFoundMsg = "No categorization scheme found for key [{0}]";
      CategorizationScheme scheme = opt.orElseThrow(
            () -> ApiUtils.raise(Response.Status.NOT_FOUND, format(notFoundMsg, key), null, null));

      switch (scheme.getType())
      {
         case TREE:
            return new TreeCategorizationResource(repo, (TreeCategorization)scheme);
         default:
            String badScheme = "The categorization strategy ({0}) of the requested scheme ({1}) is not supported.";
            throw ApiUtils.raise(Response.Status.INTERNAL_SERVER_ERROR, format(badScheme, scheme.getType(), key), null, null);
      }
   }

   private RestApiV1.Categorization createTreeCategorization(RestApiV1.CategorizationDesc categorization)
   {
      try
      {
         EditCategorizationCommand command = repo.create(CategorizationScheme.Strategy.TREE, categorization.key);
         command.setDescription(categorization.description);
         command.setLabel(categorization.label);

         String schemeId = command.execute().get();
         String logSuccessMsg = "Created new categorization scheme {0}: {1}/{2} ({3}).";
         logger.info(() -> format(logSuccessMsg, schemeId, repo.getScope().getScopeId(), categorization.key, categorization.label));

         CategorizationScheme scheme = repo.getById(schemeId);
         if (!TreeCategorization.class.isInstance(scheme))
            throw new InternalServerErrorException("An unexpected type of categorization created by the server.");

         return ModelAdapterV1.adapt((TreeCategorization)scheme);
      }
      catch (Exception e)
      {
         String template = "Failed to create new categorization scheme for key {0} (1). Unexpected error: {2}";
         throw new InternalServerErrorException(format(template, categorization.key, categorization.label, e.getMessage()));
      }
   }

   private Optional<CategorizationScheme> getByKey(String key)
   {
      try
      {
         return Optional.of(repo.get(key));
      }
      catch (IllegalArgumentException ex)
      {
         return Optional.empty();
      }
   }


   private Optional<CategorizationScheme> getById(String id)
   {
      try
      {
         return Optional.of(repo.getById(id));
      }
      catch (IllegalArgumentException ex)
      {
         return Optional.empty();
      }
   }

   private WebApplicationException raise(Response.Status status, String msg)
   {
      Response resp = Response.status(Response.Status.BAD_REQUEST)
                  .encoding("UTF-8")
                  .entity(msg)
                  .build();
      return new WebApplicationException(resp);
   }
}
