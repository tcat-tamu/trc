package edu.tamu.tcat.trc.refman;

import java.net.URI;

import edu.tamu.tcat.trc.refman.search.BibItemSearchCommand;
import edu.tamu.tcat.trc.refman.types.ItemType;
import edu.tamu.tcat.trc.refman.types.ItemTypeProvider;

/**
 * Defines a collection of bibliographic references. This is the primary interface for
 * creating, retrieving and editing bibliographic references. Collections ar
 *
 * <p>
 * Within the TRC Reference Management system, each reference belongs to exactly one
 * collection. The collection defines the item type provider that will be used by all
 * references associated with the collection. Note that, since a bibliographic reference is
 * associated with a single collection, references to the same bibliographic item may be
 * duplicated across different collections, possibly with different bibliographic information.
 * This is by design, and reflects both existing practice (different collections accessed from
 * remote sites such as Zotero or Mendely will have different references for the same item) and
 * to allow users to manage distinct groups of bibliographic material (for example, to keep a
 * personal bibliography separate from a bibliography that is shared by a group). In general,
 * duplicate references to the same item should not be present within a single collection.
 * Supporting duplicate detection and resolution, however, is an implementation detail.
 *
 * <p>
 * Collections are access controlled and may be associated with a single user, a
 * group or an application. Instances of a {@code ReferenceCollection} are obtained via the
 * {@link ReferenceCollectionManager} and encapsulate the account information of a user who
 * is authorized to access the collection. All actions will be performed within the permissions
 * scope of the user who obtains the collection instance. Consequently, collections are
 * intended to be transient rather than system services.
 */
public interface ReferenceCollection extends AutoCloseable
{
  /*
   *  -- Each bibliographic reference belongs to exactly one collection.
   *  -- instances encapsulate user account information
   *  -- intended to be transient, rather than system services
   *  -- facade over repo, search and other services
   */

   /**
    * @return A unique identifier for this collection.
    */
   String getId();

   /**
    * @return A display name for this collection.
    */
   String getName();

   /**
    * @return The item type provide that is defined for this collection. All bibliographic
    *       references from the same collection will use the same item type provider.
    */
   ItemTypeProvider getTypeProvider();

   /**
    * @return A command for use in searching the underlying collection.
    */
   @Deprecated // instead, we need to create and execute a serializable query.
   BibItemSearchCommand createSearchCommand();

   // TODO determine how to clone references from different collections

   /**
    * Retrieve a specific bibliographic reference.
    *
    * @param id The unique identifier for a bibliographic entry to return.
    *
    * @return The identified bibliographic reference.
    *
    * @throws IllegalArgumentException If the identified reference is not found.
    * @throws IllegalStateException If the underlying collection could not be accessed
    */
   BibliographicReference get(URI id) throws RefManagerException;

   /**
    * Obtain an edit command for use in creating a new bibliographic reference.
    *
    * @param type The type of bibliographic reference to create.
    * @return A command to be used to edit the reference. The reference will not be created
    *    until this command is executed. Commands that are not executed before this collection
    *    is closed will have no effect.
    * @throws IllegalArgumentException If the supplied item type is not supported by this
    *    collection.
    * @throws UnsupportedOperationException If the account currently associated with this
    *    collection does not allow updates.
    */
   EditReferenceCommand create(ItemType type);

   /**
    * Obtain an edit command to modify an existing bibliographic reference.
    *
    * @param id The id of the bibliographic reference to edit.
    * @return A command to be used to edit the reference. The reference will not be modified
    *    until this command is executed. Commands that are not executed before this collection
    *    is closed will have no effect.
    *
    * @throws IllegalArgumentException If there is no reference with the supplied id.
    * @throws UnsupportedOperationException If the account currently associated with this
    *    collection does not allow updates.
    */
   EditReferenceCommand edit(URI id);

   /**
    * Remove an existing bibliographic reference.
    *
    * @param id The id of the bibliographic reference to delete.
    * @throws IllegalArgumentException If there is no reference with the supplied id.
    * @throws UnsupportedOperationException If the account currently associated with this
    *    collection does not allow updates.
    */
   void delete(URI id);

   /**
    * @return {@code true} if the account currently being used to access this collection
    *       has permission to create and edit references within the collection.
    */
   boolean isWritable();
}
