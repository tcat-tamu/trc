package edu.tamu.tcat.trc.refman;

import java.util.List;
import java.util.Set;

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
 */
public interface BibliographicReference
{
   /**
    * @return A unique, persistent identifier for this bibliographic reference. Note that the
    *    id references this descriptive bibliographic record, not the referenced item.
    */
   String getId();

   /**
    * @return The bibliographic type of the referenced item (such as book, film, journal article).
    */
   ItemType getType();

   /**
    * @return A list of people or entities who contributed to or are otherwise responsible for
    *    the creation of this item (for example, authors).
    */
   List<CreatorValue> getCreators();

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
    * A key-value pair for a particular field of the bibliographic record. Note that this does
    * not include the creators.
    */
   public interface FieldValue
   {
      /**
       * @return The field type key.
       */
      ItemFieldType getFieldType();

      /**
       * @return The value associated with this field. May be an empty string.
       */
      String getValue();
   }

   /**
    * A structured representation of a person or entity who contributed to the creation of an
    * item. Creators require a structured representation that is not adequately supported by
    * the general key-value pair structure of other bibliographic fields.
    *
    * <p>Creators are typically defined using a family and given name and will be sorted
    * lexigraphically by the family name and then the given name. Alternatively, for some types
    * of creators such as institutions, there is not adequate structured representation of the
    * name. In these cases a single name value may be supplied.
    *
    * <p>Creator names provide a bibliographic description of a person, that is, the name of the
    * creator as it is associated with a bibliographic item. This is usually inadequate to uniquely
    * identify the individual creators as a person may use multiple names, write anonymously or
    * pseudonymously and multiple people will have the same name (J Smith, and A Jain, for instance).
    * This type provides a {@link #getAuthority()} method to (optionally) provide a identifier that
    * uniquely references the author within some authority. The authority list used and the format
    * of the identifier are defined by the application that uses the reference management API.
    */
   public interface CreatorValue
   {
      /**
       * @return A unique identifier for this person as defined by some canonical name authority.
       *       The specific choice of a name authority is determined by the client application.
       *       May be {@code null}.
       */
      String getAuthority();

      // TODO do we need to separate family name from the 'name' field or can we collapse
      //      these two representations
      /**
       * @return The role this individual played in the creation of the work, for example,
       *    author, editor, translator, contributor, etc. Additional information about this
       *    role should be obtained from the item type definition.
       */
      String getRoleId();

      /**
       * @return An unstructured representation of the creator's name.
       */
      String getName();

      /**
       * @return The creator's family or last name. This will be used as the primary value
       *    for sorting.
       */
      String getFamilyName();

      /**
       * @return The creator's given or first name. Used as a secondary value for sorting.
       */
      String getGivenName();

   }
}
