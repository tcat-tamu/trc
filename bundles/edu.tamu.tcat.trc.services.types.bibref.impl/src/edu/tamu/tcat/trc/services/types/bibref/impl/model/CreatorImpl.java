package edu.tamu.tcat.trc.services.types.bibref.impl.model;

import edu.tamu.tcat.trc.services.types.bibref.Creator;
import edu.tamu.tcat.trc.services.types.bibref.impl.repo.DataModelV1;

public class CreatorImpl implements Creator
{
   private final String role;
   private final String firstName;
   private final String lastName;
   private final String name;

   public CreatorImpl(DataModelV1.Creator dto)
   {
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
