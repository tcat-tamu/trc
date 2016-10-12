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
package edu.tamu.tcat.trc.entries.types.biblio.impl.legacy.search.copies;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.search.solr.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;

public class VolumeTextDocument
{
   // composed instead of extended to not expose TrcDocument as API to this class
   private TrcDocument indexDocument;

   public VolumeTextDocument()
   {
      indexDocument = new TrcDocument(new FullTextPageConfig());
   }

   public SolrInputDocument getDocument()
   {
      return indexDocument.build();
   }

   public static VolumeTextDocument create(String volId, String assocEntry, String text) throws SearchException
   {
      VolumeTextDocument doc = new VolumeTextDocument();

      doc.indexDocument.set(FullTextVolumeConfig.ID, volId);
      doc.indexDocument.set(FullTextVolumeConfig.TEXT, text);
      doc.indexDocument.set(FullTextVolumeConfig.ASSOC_ENTRY, assocEntry);

      return doc;
   }
}
