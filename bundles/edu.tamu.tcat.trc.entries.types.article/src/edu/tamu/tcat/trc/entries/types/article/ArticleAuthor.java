package edu.tamu.tcat.trc.entries.types.article;

import java.util.Optional;
import java.util.Set;

/**
 *  An identifier for an author of the article.
 *
 * @since 1.1
 */
public interface ArticleAuthor
{
   /**
    * @return An internal identifier for this author.
    */
   String getId();

   /**
    * @return A display name for this author..
    */
   String getDisplayName();

   /**
    * @return The first or given name of this individual.
    */
   String getFirstname();

   /**
    * @return The last or family name of this individual. Used for alphabetizing by author.
    */
   String getLastname();

   /**
    * @return The set of properties defined for this author.
    */
   Set<String> getProperties();

   /**
    * @param key The property whose value should be retrieved.
    * @return The value for the defined property.
    */
   Optional<String> getProperty(String key);
}
