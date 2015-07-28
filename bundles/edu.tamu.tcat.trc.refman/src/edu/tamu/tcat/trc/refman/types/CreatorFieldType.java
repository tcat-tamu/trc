package edu.tamu.tcat.trc.refman.types;

import java.util.Collection;

/**
 * A specialized {@link ItemFieldType} used to represent individuals or organizations that
 * played a role in the creation of the item. Each item type defines a set of valid
 * {@link CreatorRole}s (e.g., author, editor, director).
 *
 * <p>
 * The BibliographicItem API handle creator fields separately from other field types due to the
 * need to represent the internal structure of the creator's name and the role that agent played
 * in the creation of the work.
 */
public interface CreatorFieldType extends ItemFieldType
{
   /**
    * @return The roles that are involved in the creation of this items of this type.
    */
   Collection<CreatorRole> getAssociatedRoles();

}
