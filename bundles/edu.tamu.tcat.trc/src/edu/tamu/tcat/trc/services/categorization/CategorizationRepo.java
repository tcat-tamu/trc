package edu.tamu.tcat.trc.services.categorization;

import static java.text.MessageFormat.format;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.services.BasicServiceContext;
import edu.tamu.tcat.trc.services.ServiceContext;

/**
 * Provides the primary point of access for access retrieving, creating and
 * modifying categorization schemes.
 *
 * <p>
 * @see CategorizationScheme for detailed documentation on the design and intent of
 *       categorizations.
 */
public interface CategorizationRepo
{
   static final String CTX_SCOPE_ID = "scopeId";

   /**
    * Create a {@link ServiceContext} for use in obtaining instances of a categorization
    * service.
    *
    * @param account The user or system account requesting access. May be <code>null</code>.
    * @param scopeId Categorization schemes are defined relative to a scope id which
    *       identifies, for example, the user or application domain responsible for
    *       creating the scheme
    * @return A configured {@link ServiceContext}.
    */
   static ServiceContext<CategorizationRepo> makeContext(Account account, String scopeId)
   {
      Map<String, Object> props = new HashMap<>();
      props.put(CTX_SCOPE_ID, Objects.requireNonNull(scopeId));
      return new BasicServiceContext<>(CategorizationRepo.class, account, props);
   }

   /**
    * @return The context associated with this service.
    */
   ServiceContext<CategorizationRepo> getContext();

   /**
    * @return The scope id associated with this service instance.
    */
   String getScopeId();

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
    * Categorization keys are unique within the context of a {@code scopeId} provide
    * by the associated {@link ServiceContext}.
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
