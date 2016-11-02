package edu.tamu.tcat.trc.entries.types.article.repo;

/**
 *  Obtained from an {@link EditArticleCommand} in order to update properties of
 *  one of the author's of the article.
 */
public interface AuthorMutator
{
   /**
    * @return The unique identifier for the author being edited.
    */
   String getId();

   /**
    * @param name The first name of the author.
    */
   void setFirstname(String name);

   /**
    * @param name The last name of the author. This field will be used to
    *       determine sort-order, where appropriate.
    */
   void setLastname(String name);

  /**
   * @param name The display name of the author.
   */
   void setDisplayName(String name);

   /**
    * Sets the value of the given property.
    *
    * @param key The property to set.
    * @param value The associated value. May not be <code>null</code>, may be an empty string.
    */
   void setProperty(String key, String value);

   /**
    * Clears the value of the supplied property.
    * @param key The property to clear.
    */
   void clearProperty(String key);

   /**
    * Clears all properties associated with this author.
    */
   void clearProperties();
}
