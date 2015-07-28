package edu.tamu.tcat.trc.refman.types;

/**
 *  Defines a single unit of bibliographic information, such as a title, publication date
 *  or journal.
 *
 *  <p>
 *  Note the special case {@link CreatorFieldType} that is used to handle information about the
 *  author or other creator of an item. In general, authors are associated with a specific role
 *  and require special handling to represent and format their names as required by a particular
 *  citation style.
 */
public interface ItemFieldType
{

   /**
    * @return An identifier for this field type.
    */
   String getId();

   /**
    * @return Identifies a base field. This is intended for mapping more detailed fields of one
    *    item type into more general fields of another item type for the purpose of converting
    *    bibliographic items between different types.
    */
   String getFieldBase();

   /**
    * @return The semantic type of this field. This is intended for use by the UI to validate
    *    data entry. For example, types might include 'number', 'text', 'date', 'uri', 'issn',
    *    'doi', etc. Note that this should be used for hinting and support for data entry and
    *    the associated values may not correctly match the identified type, depending on the
    *    specific details of the item being described.
    */
   String getType();

   /**
    * @return The name of this field type, suitable for display.
    */
   String getLabel();

   /**
    * @return A description of this field type, suitable for display.
    */
   String getDescription();

}
