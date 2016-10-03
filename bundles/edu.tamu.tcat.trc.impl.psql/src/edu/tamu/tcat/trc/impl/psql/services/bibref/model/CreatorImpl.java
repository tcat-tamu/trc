package edu.tamu.tcat.trc.impl.psql.services.bibref.model;

import edu.tamu.tcat.trc.impl.psql.services.bibref.repo.DataModelV1;
import edu.tamu.tcat.trc.services.bibref.Creator;

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
