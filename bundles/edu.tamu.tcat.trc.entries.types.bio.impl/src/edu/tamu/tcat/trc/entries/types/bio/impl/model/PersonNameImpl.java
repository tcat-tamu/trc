package edu.tamu.tcat.trc.entries.types.bio.impl.model;

import java.util.Objects;

import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.entries.types.bio.impl.repo.DataModelV1;

public class PersonNameImpl implements PersonName
{
   private String id;
   private String title;
   private String givenName;
   private String middleName;
   private String familyName;
   private String suffix;

   private String displayName;

   public PersonNameImpl(DataModelV1.PersonName personDV)
   {
      this.id = personDV.id;
      this.title = personDV.title;
      this.givenName = personDV.givenName;
      this.middleName = personDV.middleName;
      this.familyName = personDV.familyName;
      this.suffix = personDV.suffix;

      this.displayName = personDV.displayName;
   }

   public String getId()
   {
      return id;
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
      return Objects.hash(title, givenName, middleName, familyName, suffix, displayName);
   }
}
