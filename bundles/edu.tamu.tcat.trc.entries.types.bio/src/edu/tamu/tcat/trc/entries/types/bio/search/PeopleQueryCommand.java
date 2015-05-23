package edu.tamu.tcat.trc.entries.types.bio.search;

import java.util.List;

import edu.tamu.tcat.trc.entries.types.bio.rest.v1.SimplePersonResultDV;

public interface PeopleQueryCommand
{
   List<SimplePersonResultDV> getResults() throws Exception;

   PeopleQueryCommand search(String syntheticName);

   PeopleQueryCommand byFamilyName(String familyName);

   PeopleQueryCommand setRowLimit(int rows);

}
