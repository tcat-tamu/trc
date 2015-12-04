package edu.tamu.tcat.trc.refman;

import java.util.List;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.refman.dto.CreatorDTO;
import edu.tamu.tcat.trc.refman.types.ItemFieldType;
import edu.tamu.tcat.trc.refman.types.ItemType;

/**
 * A command for use in editing a bibliographic reference.
 */
public interface EditReferenceCommand
{

   /**
    * Sets the bibliographic type of this reference. Note that this will typically be set
    * to a default value upon creation and may be modified during the editing process. In the
    * event that this is modified, the underlying fields associated with the bibligraphic
    * reference will change. Implementations should (but may not) make a best-effort attempt
    * to map previous fields into the updated item type. In general, however, conversions
    * between different reference types may result in previously assigned values being orphaned
    * (that is, values that have no corresponding field in the new type). These orphaned values
    * will not be returned by the resulting bibliographic reference. Implementations may elect
    * to persist these values for auditing purposes or to support conversion back to the
    * original reference type. This behavior, however, is implementation specific and intended
    * to serve as a convenience for the user.
    *
    * @param type The type for this bibliographic reference
    */
   void setType(ItemType type);

   /**
    * Assigns a value for a specific field.
    *
    * @param field The field for which the value will be assigned
    * @param value The value to be assigned
    *
    * @throws IllegalArgumentException If the supplied field is not defined for bibliographic
    *    entries of this type.
    */
   void setField(ItemFieldType field, String value) throws IllegalArgumentException;

   /**
    * Sets the list of creators associated with this bibliographic entry.
    * @param creators the list of creators associated with this bibliographic entry.
    */
   void setCreators(List<CreatorDTO> creators);

   /**
    * Executes this command, persisting all changes to the underlying data storage layer.
    * @return A {@link Future} that will supply the identifier of the updated bibliographic
    *    reference or throw any resulting exceptions.
    */
   Future<String> execute();

}
