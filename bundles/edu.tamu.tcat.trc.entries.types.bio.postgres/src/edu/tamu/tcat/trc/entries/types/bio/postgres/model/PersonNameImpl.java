package edu.tamu.tcat.trc.entries.types.bio.postgres.model;

import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.entries.types.bio.dto.PersonNameDTO;

public class PersonNameImpl implements PersonName
{
   private String title;
   private String givenName;
   private String middleName;
   private String familyName;
   private String suffix;

   private String displayName;

   public PersonNameImpl(PersonNameDTO personDV)
   {
      this.title = personDV.title;
      this.givenName = personDV.givenName;
      this.middleName = personDV.middleName;
      this.familyName = personDV.familyName;
      this.suffix = personDV.suffix;

      this.displayName = personDV.displayName;
   }

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