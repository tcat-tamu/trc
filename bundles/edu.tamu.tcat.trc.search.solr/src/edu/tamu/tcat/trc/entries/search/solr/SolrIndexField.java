package edu.tamu.tcat.trc.entries.search.solr;

import edu.tamu.tcat.trc.entries.search.SearchException;

public interface SolrIndexField<FieldType>
{
   String getName();
   Class<FieldType> getType();

   /**
    * Convert to a string suitable to be used as a SolrJ REST query parameter value.
    *
    * @param value
    * @return
    * @throws SearchException
    */
   String toSolrValue(FieldType value) throws SearchException;
}
