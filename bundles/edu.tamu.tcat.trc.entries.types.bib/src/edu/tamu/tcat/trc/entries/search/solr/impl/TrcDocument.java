package edu.tamu.tcat.trc.entries.search.solr.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.entries.search.solr.SolrIndexField;

public class TrcDocument
{
   protected final SolrInputDocument document;
   protected final SolrIndexConfig cfg;

   public TrcDocument(SolrIndexConfig cfg)
   {
      this.cfg = Objects.requireNonNull(cfg, "Missing solr config");
      document = new SolrInputDocument();
   }

   public SolrInputDocument getSolrDocument()
   {
      return document;
   }

   public <T> void set(SolrIndexField<T> field, T value) throws SearchException
   {
      document.addField(field.getName(), field.toSolrValue(value));
   }

   /*
    * TODO: This 'update' will only overwrite the field value with the given value.
    * Other types of updates are:
    *   "inc": increment a numeric field by a value
    *   "add": add one or more elements to a multivalue
    *   "remove": remove one or more elements from a multivalue
    */
   public <T> void update(SolrIndexField<T> field, T value) throws SearchException
   {
      HashMap<String, Object> map = new HashMap<>();
      map.put("set", field.toSolrValue(value));
      document.addField(field.getName(), map);
   }

   public <T> void update(SolrIndexField<T> field, Collection<T> value) throws SearchException
   {
      if (!cfg.getMultiValuedFields().contains(field))
         throw new IllegalArgumentException("Field is not multi-valued ["+field+"]");

      HashMap<String, Object> map = new HashMap<>();
      List<String> strs = new ArrayList<>();
      for (T v : value)
         strs.add(field.toSolrValue(v));
      map.put("set", strs);
      document.addField(field.getName(), map);
   }
}
