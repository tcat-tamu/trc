package edu.tamu.tcat.trc.entries.types.bio.repo;

public interface PersonNameMutator
{
   void setTitle(String title);
   void setGivenName(String first);
   void setMiddleName(String middle);
   void setFamilyName(String family);
   void setSuffix(String suffix);
   void setDisplayName(String displayName);
}
