package edu.tamu.tcat.trc.services.types.bibref;

public interface Creator
{
   /**
    * @return The role this creator played in the creation of a bibliographic work, e.g. author, editor, translator.
    */
   String getRole();

   /**
    * @return The creator's first or given name.
    */
   String getFirstName();

   /**
    * @return The creator's last or family name.
    */
   String getLastName();

   /**
    * @return A display value for the name of this creator. If present, this value should override the values returned by getFirstName and getLastName.
    */
   String getName();
}
