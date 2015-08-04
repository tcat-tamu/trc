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

   /**
    * @return The list of creator roles that are defined for entries of this type. Note that
    *       this is intended as a hint to support user interfaces and creators may be assigned
    *       to bibliographic references with roles that are not in list of defined roles.
    */
   List<CreatorRole> getCreatorRoles();
}
