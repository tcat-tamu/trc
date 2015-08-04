package edu.tamu.tcat.trc.refman;

import java.net.URI;
import java.util.Set;

import edu.tamu.tcat.trc.refman.types.CreatorRole;
import edu.tamu.tcat.trc.refman.types.ItemFieldType;
import edu.tamu.tcat.trc.refman.types.ItemType;

/**
 *  A structured key-value pair representation of bibliographic information. Each item is
 *  associated with a {@link ItemType} that defines the type of bibliographic object it
 *  describes (e.g., book, article, video, etc). The {@code ItemType} defines the fields that
 *  are associated with items of this type.
 *
 *  <p>
 *  Note that instances of this interface represents descriptive information about some
 *  bibliographic item rather than the item itself (that is, a bibliographic reference to
 *  <em>Hamlet</em> rather than a unique reference to the work <em>Hamlet</em>). Notably,
 *  the information recorded by a bibliographic reference may be duplicated within a single
 *  collection or across multiple collections.
 *
 *  <p>
 */
public interface BibliographicReference
{
   /**
    * @return A unique, persistent identifier for this bibliographic reference. Note that this
    *    identifies this descriptive bibliographic record, not the underlying item being
    *    referenced.
    */
   URI getId();

   /**
    * @return The bibliographic type of the referenced item (such as book, film, journal article).
    */
   ItemType getType();

   /**
    * @return Bibliographic description
    */
   Set<CreatorValue> getCreators();

   /**
    *
    * @return the value of the supplied field. Will not be {@code null}, may be an empty string
    *       if no value has been supplied for this field.
    * @throws IllegalArgumentException if the supplied field is not defined for this type of
    *       entry.
    */
   String getValue(ItemFieldType field) throws IllegalArgumentException;

   /**
    * @return The set of all values associated with this reference.
    */
   Set<FieldValue> getValues();



   /**
    * Represents
    *
    */
   public interface FieldValue
   {
      ItemFieldType getFieldType();

      String getValue();
   }

   public interface CreatorValue
   {
      CreatorRole getRole();

      boolean isStructured();

      String getName();

      String getFamilyName();

      String getGivenName();
   }
}
