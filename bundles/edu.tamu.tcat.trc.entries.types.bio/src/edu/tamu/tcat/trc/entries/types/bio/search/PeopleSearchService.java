package edu.tamu.tcat.trc.entries.types.bio.search;

import edu.tamu.tcat.trc.search.SearchException;

public interface PeopleSearchService
{
   PeopleQueryCommand createQueryCommand() throws SearchException;
}
