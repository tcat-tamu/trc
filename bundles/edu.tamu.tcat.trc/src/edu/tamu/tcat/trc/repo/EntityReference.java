package edu.tamu.tcat.trc.repo;

/**
 * Holds a reference to a persisted entity
 *
 * @param <EntityType>
 */
public interface EntityReference
{
   /**
    * @return The unique ID for the referenced entity.
    */
   String getId();

   /**
    * @return An application-specific string identifier that semantically represents the type of
    *       object to which this reference resolves. The application should use this value to look
    *       up a specific Java type (i.e. a Class<X> instance) in a registry.
    */
   String getType();
}
