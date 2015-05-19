package edu.tamu.tcat.catalogentries;

/**
 * A service for creating identifiers for catalog entries. Identifiers are guaranteed to be
 * unique within the scope of a named context, but may conflict across scopes. Contexts are
 * intended to be used to generate unique id sequences for diffrent types of catalog entries
 * (e.g., bibliographic entries and biographical entries have their own sequence of
 * identifiers).
 *
 * <p>
 * Implementations are free to provide there own identification schemes. Notably, there is
 * no guarantee that the values returned will be numeric.
 */
public interface IdFactory
{
   
   // Twitter has encountered a similar need for generating coherent IDs and has developed Snowflake:
   // https://blog.twitter.com/2010/announcing-snowflake
   
   /**
    * Generate a new id for the named context.
    *
    * @param context The context for the id to be generated.
    * @return A new unique identifier for the supplied context.
    */
   String getNextId(String context);
}
