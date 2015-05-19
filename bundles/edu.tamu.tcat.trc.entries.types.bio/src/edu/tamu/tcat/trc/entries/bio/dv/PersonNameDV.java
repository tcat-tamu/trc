package edu.tamu.tcat.trc.entries.bio.dv;

import edu.tamu.tcat.trc.entries.bio.PersonName;

public class PersonNameDV
{
   public String title;
   public String givenName;
   public String middleName;
   public String familyName;
   public String suffix;

   public String displayName;

   /**
    * Create a new data vehicle from the supplied {@link PersonName}.
    */
   public static PersonNameDV create(PersonName name)
   {
      PersonNameDV dto = new PersonNameDV();

      dto.title = name.getTitle();
      dto.givenName = name.getGivenName();
      dto.middleName = name.getMiddleName();
      dto.familyName = name.getFamilyName();
      dto.suffix = name.getSuffix();

      dto.displayName = name.getDisplayName();

      return dto;
   }


   public static PersonNameImpl instantiate(PersonNameDV personDV)
   {
      PersonNameImpl name = new PersonNameImpl();
      name.title = personDV.title;
      name.givenName = personDV.givenName;
      name.middleName = personDV.middleName;
      name.familyName = personDV.familyName;
      name.suffix = personDV.suffix;

      name.displayName = personDV.displayName;

      return name;
   }


   public static class PersonNameImpl implements PersonName
   {
      private String title;
      private String givenName;
      private String middleName;
      private String familyName;
      private String suffix;

      private String displayName;


      @Override
      public String getTitle()
      {
         return title;
      }

      @Override
      public String getGivenName()
      {
         return givenName;
      }

      @Override
      public String getMiddleName()
      {
         return middleName;
      }

      @Override
      public String getFamilyName()
      {
         return familyName;
      }

      @Override
      public String getSuffix()
      {
         return suffix;
      }

      @Override
      public String getDisplayName()
      {
         return displayName;
      }

   }
}
