package edu.tamu.tcat.trc.entries.types.bio.postgres;

import edu.tamu.tcat.trc.entries.types.bio.repo.PersonNameMutator;
import edu.tamu.tcat.trc.entries.types.bio.postgres.DataModelV1.PersonName;
import edu.tamu.tcat.trc.repo.ChangeSet;

public class PersonNameMutatorImpl implements PersonNameMutator
{
   private final ChangeSet<PersonName> changes;
   
   public PersonNameMutatorImpl(ChangeSet<PersonName> partial)
   {
      this.changes = partial;
   }
   
   @Override
   public void setTitle(String title)
   {
      changes.add("Set title", name -> name.title = title);
   }

   @Override
   public void setGivenName(String first)
   {
      changes.add("Set given", name -> name.givenName = first);
   }

   @Override
   public void setMiddleName(String middle)
   {
      changes.add("Set middle", name -> name.middleName = middle);
   }

   @Override
   public void setFamilyName(String family)
   {
      changes.add("Set family", name -> name.familyName = family);
   }

   @Override
   public void setSuffix(String suffix)
   {
      changes.add("Set suffix", name -> name.suffix = suffix);
   }

   @Override
   public void setDisplayName(String displayName)
   {
      changes.add("Set Display Name", name -> name.displayName = displayName);
   }

}
