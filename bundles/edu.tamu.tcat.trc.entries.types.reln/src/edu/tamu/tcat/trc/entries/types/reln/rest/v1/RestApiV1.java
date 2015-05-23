package edu.tamu.tcat.trc.entries.types.reln.rest.v1;

import java.util.Set;

public class RestApiV1
{
   /**
    * Simple data vehicle for reporting relationship identifiers as JSON objects.
    */
   public static class RelationshipId
   {
      public String id;
   }

   /**
    *  A JSON serializable representation of a {@link edu.tamu.tcat.trc.entries.types.reln.RelationshipType} for use in the REST API.
    */
   public static class RelationshipType
   {
      /**
      *  A string that uniquely identifies this relationship.
      *  @see RelationshipType#getIdentifier()
      */
      public String identifier;

      /**
      * The title of this relationship for display.
      * @see RelationshipType#getTitle()
      */
      public String title;

      /**
      * The reverse reading direction title of this relationship for display.
      * @see RelationshipType#getReverseTitle()
      */
      public String reverseTitle;

      /**
      * A textual description of the intended meaning of this relationship type.
      * @see RelationshipType#getDescription()
      */
      public String description;

      /**
      * {@code true} If this relationship is directed, {@code false} otherwise.
      * @see RelationshipType#isDirected()
      */
      public boolean isDirected;
   }
   
   public static class Relationship
   {
      public String id;
      public String typeId;
      public String description;
      public String descriptionMimeType;
      public Provenance provenance;
      public Set<Anchor> relatedEntities;
      public Set<Anchor> targetEntities;
   }
   
   public static class Anchor
   {
      public Set<String> entryUris;
   }
   
   public static class Provenance
   {
      /** The string-valued URIs associated with the creators of the associated annotation. */
      public Set<String> creatorUris;

      /** Date created in ISO 8601 format such as '2011-12-03T10:15:30Z' */
      public String dateCreated;

      /** Date modified in ISO 8601 format such as '2011-12-03T10:15:30Z' */
      public String dateModified;
   }
   
   

}
