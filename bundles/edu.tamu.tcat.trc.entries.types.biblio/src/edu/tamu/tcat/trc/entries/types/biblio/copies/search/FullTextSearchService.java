package edu.tamu.tcat.trc.entries.types.biblio.copies.search;

import edu.tamu.tcat.trc.search.SearchException;

public interface FullTextSearchService
{

   VolumeSearchCommand getVolumeSearchCommand() throws SearchException;

   PageSearchCommand getPageSearchCommand() throws SearchException;

}
