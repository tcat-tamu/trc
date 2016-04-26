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
package edu.tamu.tcat.trc.entries.types.bio.search.solr;

import java.util.function.Function;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.search.BioSearchProxy;
import edu.tamu.tcat.trc.search.SearchException;

/**
 * Represents a document in the SOLR search index. Exports its representation as
 * a {@link SolrInputDocument}, which includes a {@link BioSearchProxy} DTO as one of the fields.
 *
 * @see {@link BioSearchProxy} which is the DTO stored in one of the fields of this proxy.
 */
public class BioDocument
{
   // TODO refactor this to extract properties and from a Person and store them in a format that
   //      can be applied (or not) as needed. Should allow for updates (as needed) as well as
   //      wholesale splatting of data.

   // composed instead of extended to not expose TrcDocument as API to this class
   private final SolrInputDocument doc;

   public BioDocument(SolrInputDocument doc)
   {
      this.doc = doc;
   }

   public SolrInputDocument getDocument()
   {
      return doc;
   }

   public static BioDocument create(Person person, Function<String, String> sentenceParser) throws SearchException
   {
      SolrDocAdapter adapter = new SolrDocAdapter(sentenceParser);
      return new BioDocument(adapter.apply(person));
   }
}
