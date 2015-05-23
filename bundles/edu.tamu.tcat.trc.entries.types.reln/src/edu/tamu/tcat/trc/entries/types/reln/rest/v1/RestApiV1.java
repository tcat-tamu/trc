package edu.tamu.tcat.trc.entries.types.reln.rest.v1;

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

}
