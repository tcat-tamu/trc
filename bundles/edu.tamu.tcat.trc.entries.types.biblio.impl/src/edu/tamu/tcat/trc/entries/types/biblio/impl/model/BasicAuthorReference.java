package edu.tamu.tcat.trc.entries.types.biblio.impl.model;

import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.impl.repo.DataModelV1;

public class BasicAuthorReference implements AuthorReference
{
   private final String id;
   private final String firstName;
   private final String lastName;
   private final String role;

   public BasicAuthorReference(DataModelV1.AuthorReferenceDTO dto)
   {
      this.id = dto.authorId;
      this.firstName = dto.firstName;
      this.lastName = dto.lastName;
      this.role = dto.role;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String getFirstName()
   {
      return firstName;
   }

   @Override
   public String getLastName()
   {
      return lastName;
   }

   @Override
   public String getRole()
   {
      return role;
   }
}