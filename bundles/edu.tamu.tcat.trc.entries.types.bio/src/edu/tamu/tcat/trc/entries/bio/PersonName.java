package edu.tamu.tcat.trc.entries.bio;

/**
 * A structured representation of a person's name.  
 */
public interface PersonName
{
   /**
    * @return The title of address, such as Dr., Mr., Ms.
    */
   String getTitle();
   
   /**
    * @return The prerson's given or first name.
    */
   String getGivenName();
   
   /**
    * 
    * @return A middle name.
    */
   String getMiddleName();

   /**
    * @return The person's family or last name.
    */
   String getFamilyName();
   
   /**
    * @return A suffix such as Jr. or MD
    */
   String getSuffix();
   
   /**
    * @return The person's full name as it should be commonly displayed.
    */
   String getDisplayName();
   
}
