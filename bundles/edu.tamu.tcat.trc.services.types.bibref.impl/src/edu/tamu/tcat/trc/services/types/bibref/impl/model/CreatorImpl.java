package edu.tamu.tcat.trc.services.types.bibref.impl.model;

import edu.tamu.tcat.trc.services.types.bibref.Creator;
import edu.tamu.tcat.trc.services.types.bibref.impl.repo.DataModelV1;

public class CreatorImpl implements Creator
{
   private String role;
   private String firstName;
   private String lastName;
   private String name;

   public CreatorImpl()
   {
   }

   public CreatorImpl(String role, String firstName, String lastName, String name)
   {
      this.role = role;
      this.firstName = firstName;
      this.lastName = lastName;
      this.name = name;
   }

   public CreatorImpl(Creator other)
   {
      if (other == null)
         return;

      role = other.getRole();
      firstName = other.getFirstName();
      lastName = other.getLastName();
      name = other.getName();
   }

   public CreatorImpl(DataModelV1.Creator dto)
   {
      if (dto == null)
         return;

      role = dto.role;
      firstName = dto.firstName;
      lastName = dto.lastName;
      name = dto.name;
   }

   @Override
   public String getRole()
   {
      return role;
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
   public String getName()
   {
      return name;
   }

}
