package edu.tamu.tcat.trc.entries.types.bio.repo;

/**
 * Mutator to update the structured representation of a person's name.
 */
public interface PersonNameMutator
{
   /**
    * @param title The person's title (e.g. Mr., Dr.)
    */
   void setTitle(String title);

   /**
    * @param first The person's given or first name.
    */
   void setGivenName(String name);

   /**
    * @param middle The person's middle name.
    */
   void setMiddleName(String middle);

   /**
    * @param family The person's family or last name.
    */
   void setFamilyName(String family);

   /**
    * @param suffix A suffix (e.g., Esq. Ph.D.) to be used following the person's name.
    */
   void setSuffix(String suffix);

   /**
    * @param displayName A free-text name to display for this person.
    */
   void setDisplayName(String displayName);
}
