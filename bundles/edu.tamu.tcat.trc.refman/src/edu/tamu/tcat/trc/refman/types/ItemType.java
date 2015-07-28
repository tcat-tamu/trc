package edu.tamu.tcat.trc.refman.types;

import java.util.List;

/**
 * Describes the fields and basic display information that defines a particular type of
 * bibliographic item.
 *
 * @see ItemTypeProvider for additional detail.
 *
 */
public interface ItemType
{

   /**
    * @return A identifier for this type of bibliographic item within the context
    *    of its corresponding {@link ItemTypeProvider}.
    */
   String getId();

   /**
    * @return A label for this type of bibliographic item, suitable for display.
    */
   String getLabel();

   /**
    * @return A description of this bibliographic item.
    */
   String getDescription();

   /**
    * @return The fields associated with this type of bibliographic item in the preferred
    *       display order.
    */
   List<ItemFieldType> getFields();
}
