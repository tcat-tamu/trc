package edu.tamu.tcat.trc.search.solr;

import java.util.Collection;

import org.apache.solr.common.SolrInputDocument;

public interface DocumentBuilder
{

   <T> void set(SolrIndexField<T> field, T value) throws SearchException;

   /*
    * TODO: This 'update' will only overwrite the field value with the given value.
    * Other types of updates are:
    *   "inc": increment a numeric field by a value
    *   "add": add one or more elements to a multivalue
    *   "remove": remove one or more elements from a multivalue
    */
   <T> void update(SolrIndexField<T> field, T value) throws SearchException;

   /*
    * TODO: This 'update' will only overwrite the field value with the given value.
    * Other types of updates are:
    *   "inc": increment a numeric field by a value
    *   "add": add one or more elements to a multivalue
    *   "remove": remove one or more elements from a multivalue
    */
   <T> void update(SolrIndexField<T> field, Collection<T> value) throws SearchException;

   SolrInputDocument build();
}
