package edu.tamu.tcat.trc.entries.types.bio.postgres.model;

import java.util.Objects;

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

   @Override
   public boolean equals(Object obj)
   {
      if (!PersonName.class.isInstance(obj))
         return false;

      PersonName other = (PersonName)obj;
      return Objects.equals(title, other.getTitle())
          && Objects.equals(givenName, other.getGivenName())
          && Objects.equals(middleName, other.getMiddleName())
          && Objects.equals(familyName, other.getFamilyName())
          && Objects.equals(suffix, other.getSuffix())
          && Objects.equals(displayName, other.getDisplayName());
   }

   @Override
   public int hashCode()
   {
      int result = 17;
      result = 31 * result + title.hashCode();
      result = 31 * result + givenName.hashCode();
      result = 31 * result + middleName.hashCode();
      result = 31 * result + familyName.hashCode();
      result = 31 * result + suffix.hashCode();
      result = 31 * result + displayName.hashCode();

      return result;
   }
}
