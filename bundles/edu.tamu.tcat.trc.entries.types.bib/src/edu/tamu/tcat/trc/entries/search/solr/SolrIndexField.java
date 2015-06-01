package edu.tamu.tcat.trc.entries.search.solr;

import edu.tamu.tcat.trc.entries.search.SearchException;

public interface SolrIndexField<FieldType>
{
   String getName();
   Class<FieldType> getType();

   String toSolrValue(FieldType value) throws SearchException;
}
