package edu.tamu.tcat.trc.services.types.bibref.repo;

import java.util.List;
import java.util.Map;

public interface BibliographicItemMutator
{
   /**
    * @return The unique identifier for the bibliographic item being edited.
    */
   String getId();

   /**
    * Sets the type of work referenced by this bibliographic item.
    * @param type
    */
   void setType(String type);

   /**
    * @return A mutator to edit values on this bibliographic item's metadata.
    */
   BibliographicItemMetaMutator editMetadata();

   /**
    * Adds a creator to this bibliographic item's list of creators.
    * @param id The id of the creator to create.
    * @return A mutator to set values on the new creator item.
    */
   void setCreators(List<Creator> creators);

   /**
    * Sets an individual bibliographic item field to the specified value.
    * @param field
    * @param value
    */
   void setField(String field, String value);

   /**
    * Removes the specified field from the bibliographic item's field mapping.
    * @param field The field to remove.
    */
   void unsetField(String field);

   /**
    * Sets all field-value pairs in the provided map, adding new fields and removing missing fields.
    * @param fields
    */
   void setAllFields(Map<String, String> fields);

   public static class Creator
   {
      public String role;
      public String firstName;
      public String lastName;
      public String name;
   }
}
