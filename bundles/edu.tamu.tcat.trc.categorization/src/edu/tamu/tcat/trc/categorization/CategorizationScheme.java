package edu.tamu.tcat.trc.categorization;

import edu.tamu.tcat.trc.categorization.rest.CategorizationSchemesResource;

/**
 *  A categorization scheme is a user-defined structure designed to organize or order
 *  a particular sub-set of resources within a thematic research collection (TRC).
 *  For example, a categorization could be used to define a set of key bibliographic
 *  entries for readers new to a particular topic to start reading, a hierarchy of
 *  articles that provide encyclopedic coverage of a particular domain, or a set of
 *  people that a particular user wants to learn more about.
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
 */
public interface CategorizationScheme
{
   // TODO this probably needs to be more easily extensible at some point.
   public enum Strategy {
      SET, LIST, TREE
   }

   /**
    * @return The unique id for the taxonomy.
    */
   String getId();

   /**
    * @return The id of the scope that this categorization belongs to. Scopes are
    *    used to provide convenient organization of related categorizations, for
    *    example, user defined categorizations.
    */
   String getScopeId();

   /**
    * @return A supplied label that uniquely identifies this categorization within
    *    its corresponding scope. This value may or may not be human readable.
    */
   String getKey();

   /**
    * @return The type of this categorization.
    */
   Strategy getType();

   /**
    * @return The title of the taxonomy.
    */
   String getLabel();

   /**
    * @return The description of the taxonomy.
    */
   String getDescription();
}
