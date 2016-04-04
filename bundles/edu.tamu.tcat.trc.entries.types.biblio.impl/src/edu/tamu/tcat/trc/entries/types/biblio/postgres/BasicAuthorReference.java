package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;

public class BasicAuthorReference implements AuthorReference
{
   String id;
   String firstName;
   String lastName;
   String role;

   public BasicAuthorReference(String id,
                               String firstName,
                               String lastName,
                               String role)
   {
      this.id = id;
      this.firstName = firstName;
      this.lastName = lastName;
      this.role = role;
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