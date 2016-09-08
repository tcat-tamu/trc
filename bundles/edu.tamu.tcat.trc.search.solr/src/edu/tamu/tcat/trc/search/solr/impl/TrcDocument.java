/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.search.solr.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.DocumentBuilder;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;

public class TrcDocument implements DocumentBuilder
{
   protected final SolrInputDocument document;
   protected final SolrIndexConfig cfg;

   public static final String UPDATE_SET = "set";

   public TrcDocument(SolrIndexConfig cfg)
   {
      this.cfg = Objects.requireNonNull(cfg, "Missing solr config");
      this.document = new SolrInputDocument();
   }

   @Override
   public SolrInputDocument build()
   {
      return document;
   }

   @Override
   public <T> void set(SolrIndexField<T> field, T value) throws SearchException
   {
      document.addField(field.getName(), field.toSolrValue(value));
   }

   // Not yet implemented: need to handle cases where the collection contains nulls or empty strings
//   public <T> void set(SolrIndexField<T> field, Collection<T> value) throws SearchException
//   {
//      for (T v : value)
//         document.addField(field.getName(), field.toSolrValue(v));
//   }

   /*
    * TODO: This 'update' will only overwrite the field value with the given value.
    * Other types of updates are:
    *   "inc": increment a numeric field by a value
    *   "add": add one or more elements to a multivalue
    *   "remove": remove one or more elements from a multivalue
    */
   @Override
   public <T> void update(SolrIndexField<T> field, T value) throws SearchException
   {
      HashMap<String, Object> map = new HashMap<>();
      map.put(UPDATE_SET, field.toSolrValue(value));
      document.addField(field.getName(), map);
   }

   /*
    * TODO: This 'update' will only overwrite the field value with the given value.
    * Other types of updates are:
    *   "inc": increment a numeric field by a value
    *   "add": add one or more elements to a multivalue
    *   "remove": remove one or more elements from a multivalue
    */
   @Override
   public <T> void update(SolrIndexField<T> field, Collection<T> value) throws SearchException
   {
      if (!cfg.getMultiValuedFields().contains(field))
         throw new IllegalArgumentException("Field is not multi-valued ["+field+"]");

      HashMap<String, Object> map = new HashMap<>();
      List<String> strs = new ArrayList<>();
      for (T v : value)
         strs.add(field.toSolrValue(v));
      map.put(UPDATE_SET, strs);
      document.addField(field.getName(), map);
   }
}
