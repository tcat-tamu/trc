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
package edu.tamu.tcat.trc.search.solr;

import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;

import edu.tamu.tcat.trc.SearchException;

/**
 * An application-specific configuration for an indexer and query builder implementation to use for
 * initialization, validation, and execution.
 * <p>
 * Instances should be stateless and may be constructed as needed for short lifecycle.
 */
public interface SolrIndexConfig
{
   /**
    * Allows the SOLR configuration to supply initial query parameters to be used by all
    * queries. For example, this could be used to programmatically define facets to be used
    * by default or to override the default query type.
    *
    * <p>
    * In general, default query parameters should be done through the use of the SOLR core's
    * XML configuration file rather than through use of the REST-based query API. Consequently,
    * this method should be used to set values that need to differ across application defined
    * query configurations that will be executed against the same underlying document core.
    * This method can also be useful when defining a search client to be executed against a
    * SOLR installation that the application developer does not have permission to configure.
    *
    * <p>A default, no-op implementation of this method is provided to support the anticipated
    * common case.
    *
    * @param params The query parameters that will be used to define the query being built.
    * @throws SearchException If the query parameters cannot be initialized.
    */
   default void initialConfiguration(SolrQuery params) throws SearchException
   {
      // default impl is no-op
   }

   /**
    * Set (or override) configuration for the "basic" search criteria. Since
    * a "basic" search may search with different boosts over a custom set of fields,
    * the implementation must decide how to apply the search query across the index.
    *
    * @param q
    * @param params
    * @throws SearchException
    */
   default void configureBasic(String q, SolrQuery params) throws SearchException
   {
      throw new UnsupportedOperationException("Basic configuration not supported");
   }

   /**
    * The DTO type to use for a "search proxy", which is a serialized JSON data transfer
    * object stored in the index as a literal and retrieved as a single value to be the
    * representative for the object stored in the search index.
    */
   Class<?> getSearchProxyType();

   @Deprecated
   default Class<?> getIndexDocumentType() {
      throw new UnsupportedOperationException();
   }

   /*
    * These methods are here in the config and not a part of every field to allow more
    * flexible implementation of the properties and less overhead per field instance.
    */
   Collection<? extends SolrIndexField<?>> getIndexedFields();
   Collection<? extends SolrIndexField<?>> getStoredFields();
   Collection<? extends SolrIndexField<?>> getMultiValuedFields();
}
