package edu.tamu.tcat.trc.services.categorization;

import static java.text.MessageFormat.format;

import java.util.concurrent.CompletableFuture;

/**
 * Provides the primary point of access for access retrieving, creating and
 * modifying categorization schemes within a given {@link CategorizationScope}.
 * Instances of a {@link CategorizationRepo} may be obtained from a
 * {@link CategorizationRepoFactory} which is instantiated in an application
 * dependent manner (for example, as an OSGi declarative service).
 *
 * <p>
 * @see CategorizationScheme for detailed documentation on the design and intent of
 *       categorizations.
 */
public interface CategorizationRepo
{
   /**
    * @return The scope for which this repo is defined.
    */
   CategorizationScope getScope();

   /**
    * Determines if the supplied key is currently used by a categorization scheme within
    * this scope.
    *
    * <p>Note that this is intended to support basic checks and to support more clear user
    * interfaces. In general, attempts to access a scheme for an existing key or to create a
    * new scheme with an unused key may fail following a test for existence using this method
    * due to concurrent updates. Callers must be prepared to deal with these scenarios
    * appropriately. In most use cases, however, these conflicts will be sufficiently rare
    * that this method can be used to provide valuable feedback.
    *
    * @param key The key to test.
    * @return <code>true</code> if the supplied key is currently used to identify a
    *       categorization scheme.
    */
   default boolean isUsed(String key)
   {
      throw new UnsupportedOperationException();
   };

   /**
    * Retrieves a specific {@link CategorizationScheme} using the user-supplied key.
    * Categorization keys are unique within a given {@link CategorizationScope}.
    *
    * <p>Note that these keys are suitable in many contexts but can and do change over
    * time. Consequently they are not suitable for use as persistent identifiers.
    * For persistent identification of a categorization, use {@link #getById(String)}.
    *
    * @param key The key of the categorization to retrieve.
    * @return The identified categorization.
    */
   CategorizationScheme get(String key) throws IllegalArgumentException;

   /**
    * Retrieves a {@link CategorizationScheme} using a persistent id.
    *
    * @param id The persistent identifier for the categorization.
    * @return The identified categorization
    */
   CategorizationScheme getById(String id) throws IllegalArgumentException;

   /**
    * Retrieves a {@link CategorizationScheme} using a persistent id.
    *
    * @param id The persistent identifier for the categorization.
    * @param type The Java type of the anticipated scheme. Should be one of a limited
    *       number of interfaces that define the various categorization strategies.
    * @return The identified categorization
    */
   default <S extends CategorizationScheme> S getById(String id, Class<S> type)
   {
      String errmsg = "The categorization scheme {0} is not and instance of {1}";

      CategorizationScheme cmd = getById(id);
      if (!type.isInstance(cmd))
         throw new IllegalArgumentException(format(errmsg, id, type));

      return type.cast(cmd);
   }

   /**
    * Used to create a new categorization. Note that changes will not be applied
    * until the create method of the categorization command is executed.
    *
    * @param strategy The categorization strategy to be use for this scheme.
    * @param key A user-defined key for use in identifying this categorization.
    *    This key must be unique with the categorization scope associated with this
    *    repo.
    *
    * @return an edit command for creating a new categorization.
    */
   EditCategorizationCommand create(CategorizationScheme.Strategy strategy, String key);

   /**
    * Edits an existing categorization.
    *
    * @param id The id of the categorization to edit
    * @return an edit command for making changed to an existing categorization.
    */
   EditCategorizationCommand edit(String id);

   /**
    * Edits an existing categorization.
    *
    * @param id The id of the categorization to edit
    * @param type The Java type of the anticipated edit command
    * @return an edit command for making changed to an existing categorization.
    */
   default <CMD extends EditCategorizationCommand> CMD edit(String id, Class<CMD> type)
   {
      String errmsg = "The categorization scheme {0} is not associated with the requested edit command {1}";

      EditCategorizationCommand cmd = edit(id);
      if (!type.isInstance(cmd))
         throw new IllegalArgumentException(format(errmsg, id, type));

      return type.cast(cmd);
   }

   /**
    * Remove a categorization.
    *
    * @param id The id of the categorization to remove
    * @return A future that resolves once the categorization has been removed.
    *    Will return <code>true</code> if the identified category was found and
    *    removed a <code>false</code> if it was not found. If there errors were
    *    encountered, these will be propagated.
    */
   CompletableFuture<Boolean> remove(String id);
}
