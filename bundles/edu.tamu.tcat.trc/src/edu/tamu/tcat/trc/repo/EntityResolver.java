package edu.tamu.tcat.trc.repo;

/**
 * Fetches persisted entities as referenced by arbitrary {@link EntityReference}s.
 * @deprecated use EntryReference instead
 */
@Deprecated
public interface EntityResolver
{
   /**
    * Resolves an {@link EntityReference} into the domain object that it represents.
    *
    * @param reference A reference to a persisted entity.
    * @return The referenced entity.
    * @throws IllegalArgumentException If the reference refers to an unrecognized type or an entity
    *       that cannot be loaded.
    */
   <EntityType> EntityType resolve(EntityReference reference, Class<? super EntityType> type) throws IllegalArgumentException;
}
