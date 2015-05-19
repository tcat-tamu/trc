package edu.tamu.tcat.trc.entries.bio;

import java.util.List;

import edu.tamu.tcat.trc.entries.bio.rest.v1.SimplePersonResultDV;

public interface PeopleQueryCommand
{
   public abstract List<SimplePersonResultDV> getResults();

   public abstract PeopleQueryCommand search(String syntheticName);

   public abstract PeopleQueryCommand byFamilyName(String familyName);

   public abstract PeopleQueryCommand setRowLimit(int rows);

}
