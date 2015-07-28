package edu.tamu.tcat.trc.refman.types;

/**
 * Indicates what role a particular creator played in the creation of an item. Allowable roles
 * for a given type of bibliographic item are defined by the {@link CreatorFieldType} of the
 * associated {@link ItemType}.
 */
public interface CreatorRole
{
   /**
    * @return The system identifier for this role. '
    */
   String getId();

   /**
    * @return A label for this role, suitable for display.
    */
   String getLabel();

}
