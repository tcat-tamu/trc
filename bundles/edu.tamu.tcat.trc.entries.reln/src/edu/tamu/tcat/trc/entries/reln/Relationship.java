package edu.tamu.tcat.trc.entries.reln;


/**
 *  Represents a relationship of some type between two or more entities within the Catalog Entries
 *  framework. Relationships are used to provide editorial or scholarly commentary on the
 *  intellectual connections between related items. For example, a relationship might be used to
 *  indicate that a particular book was written as a refutation of an argument presented in another
 *  book.
 *
 *  <p>
 *  Commonly, relationships will be ordered pairs (one work was influenced by another) but may
 *  represent more complex structure (four works developed a similar idea in slightly different
 *  ways without any clear dependence on each other). The specific interpretation of the related
 *  entries will be governed by the {@link RelationshipType} of this {@link Relationship}.
 *
 *  <p>
 *  Relationships may be directed or undirected. A directed relationship will have a non-empty
 *  destination {@link AnchorSet}. Directionality should be understood to indicate that one entry
 *  (or set of entries) is related as described by the {@link RelationshipType} to another entry
 *  (or set of entries) and implies that the source entry is somehow about the destination entry.
 *  An undirected relationship indicates merely that, in the judgment of the relationship creator,
 *  two or more entries are related without implying 'aboutness' or intentionality (see
 *  {@link http://plato.stanford.edu/entries/intentionality/}) among the related entries.
 *
 *  <p>
 *  Relationships reference {@link Anchor}s rather than catalog entries directly. This allows the
 *  relationship to describe one-to-many links and other more complex link structures (for example,
 *  to indicate that one book was responding to an idea that has been variously presented in several
 *  different works). It also allows relationship to reference the internal structure of their
 *  referent, for example a page range within an edition or a digital copy of a book, a span of text,
 *  a region within an image, etc.
 */
public interface Relationship
{
   // TODO integrate with TBD bibliography module to provide bibliographic justification for the relationship

   /**
    * @return The unique identifier for this relationship.
    */
   String getId();

   /**
    * @return The type of this relationship. All relationships are typed. The specific
    *    relationship types that are available is determined by the configuration and
    *    deployment of a particular application to suite the editorial needs of the
    *    project. The relationship types typically define (either implicitly or explicitly)
    *    the types of entities that may be related, whether or not relationships are
    *    directed and various other properties of the relationship that are used to
    *    control its display in user interfaces, influence its the interpretation in
    *    automated reasoning systems, and support refined querying and filtering.
    */
   RelationshipType getType();

   /**
    * @return A narrative description of this relationship. This description may
    *       contain light markup using HTML or another format. The format used
    *       will be described by {@link #getDescriptionFormat()}.
    */
   String getDescription();

   /**
    * @return The mime type of the description content.
    */
   String getDescriptionFormat();

   /**
    * @return details about who is responsible for the intellectual content of this
    *    {@link Relationship} and when it was created. Note that this
    */
   Provenance getProvenance();

   /**
    * @return A set of {@link Anchor}s to the catalog entries described by this relationship.
    */
   AnchorSet getRelatedEntities();

   /**
    *  @return For directed relationships, returns the entities that are being referenced.
    *    For undirected relationships, returns {@code null} or an empty {@code AnchorSet}.
    */
   AnchorSet getTargetEntities();
}
