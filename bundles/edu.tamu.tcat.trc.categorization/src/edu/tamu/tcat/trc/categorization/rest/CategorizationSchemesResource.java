package edu.tamu.tcat.trc.categorization.rest;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

   // HACK for initial API development, we'll store these internally

   /**
    *  Create a new categorization.
    *
    * @param key The identifier for this categorization.
    * @return A created response with a link to the created categorization. The created
    *       categorization will be supplied in the body.
    */
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   public Response createCategorization(RestApiV1.Categorization categorization)
   {
      if (!categorization.type.equals(HierarchicalCategorizationResource.TYPE))
         throw new BadRequestException("Cannot create new categorization. "
               + "Categorizatons of the requested type " + categorization.type + " are not supported.");

      /// TODO throws conflict - if the resource already exists
      //              bad request invalid type
      //              forbidden
      throw new UnsupportedOperationException();
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
    * @throws NotFoundException If no categorization is defined for the provided key.
    */
   @Path("/")
   public CategorizationResource getCategorization(@QueryParam("key") String key)
   {
      // NOTE for simplicity, let's use a suffix to indicate the categorization type?
      return new CategorizationResource(key);
   }

   /**
    * Looks up a categorization by its persistent, internal identifier and (optionally) a version id.
    *
    * @param id The id of the categorization scheme
    * @param version The version number to return (optional)
    * @return The requested version.
    * @throws NotFoundException If the identified categorization resource could not be found.
    */
   @Path("/")
   public CategorizationResource getCategorizationById(@MatrixParam("id") @NotNull String id,
                                                       @MatrixParam("v") @DefaultValue("-1") long version)
   {
      throw new UnsupportedOperationException();
   }
}
