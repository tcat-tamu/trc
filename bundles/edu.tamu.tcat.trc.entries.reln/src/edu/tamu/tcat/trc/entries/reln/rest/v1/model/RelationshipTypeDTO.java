package edu.tamu.tcat.trc.entries.reln.rest.v1.model;

import edu.tamu.tcat.trc.entries.reln.RelationshipType;

/**
 *  A JSON serializable representation of a RelationshipType for use in the REST API.
 */
public class RelationshipTypeDTO
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

  public static RelationshipTypeDTO create(RelationshipType type)
  {
     RelationshipTypeDTO dto = new RelationshipTypeDTO();
     dto.identifier = type.getIdentifier();
     dto.title = type.getTitle();
     dto.reverseTitle = type.getReverseTitle();
     dto.description = type.getDescription();
     dto.isDirected = type.isDirected();

     return dto;
  }
}
