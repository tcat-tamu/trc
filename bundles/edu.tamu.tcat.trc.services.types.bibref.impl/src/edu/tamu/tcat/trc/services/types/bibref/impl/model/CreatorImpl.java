package edu.tamu.tcat.trc.services.types.bibref.impl.model;

import edu.tamu.tcat.trc.services.types.bibref.Creator;

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

      this.role = other.getRole();
      this.firstName = other.getFirstName();
      this.lastName = other.getLastName();
      this.name = other.getName();
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
