package edu.tamu.tcat.trc.refman.types;

import java.util.Collection;

/**
 *  Provides access to a set of {@link ItemType}s that will be used to define the fields
 *  associated with bibliographic items of various types. In general, bibliographic items
 *  can be of many different types (e.g., Books, Journal Articles, Blog Entries, Films, etc).
 *  Bibliographic information about these items is structured by an ordered set of fields. For
 *  example, a Film might have information about it's running time while a journal article may
 *  supply the name of journal it was published in.
 *
 */
public interface ItemTypeProvider
{

   /**
    * @return A set of unique fields defined by this {@code ItemTypeProvider}. Field types
    *       definitions are shared across {@code ItemType}s defined by the same
    *       {@code ItemTypeProvider}.
    */
   Collection<ItemFieldType> listDefinedFields();

   /**
    * @return All bibliographic item types defined by this provider.
    */
   Collection<ItemType> listDefinedTypes();

   /**
    * @param typeId The id of the type to return.
    * @return The identified bibliographic item type.
    * @throws IllegalArgumentException If the requested item type is not defined by this
    *       provider.
    */
   ItemType getItemType(String typeId) throws IllegalArgumentException;
}
