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
package edu.tamu.tcat.trc.entries.types.bib.copies.search.solr;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.types.biblio.copies.search.PageSearchProxy;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;

public class PageTextDocument
{
   // composed instead of extended to not expose TrcDocument as API to this class
   private TrcDocument indexDocument;

   private PageSearchProxy proxy;

   public PageTextDocument()
   {
      indexDocument = new TrcDocument(new FullTextPageConfig());
   }

   public SolrInputDocument getDocument()
   {
      return indexDocument.getSolrDocument();
   }

   public String getText()
   {
      return proxy.pageText;
   }

   public static PageTextDocument create(String pgId, int seqNo, String text) throws SearchException
   {
      PageTextDocument doc = new PageTextDocument();

      doc.indexDocument.set(FullTextPageConfig.ID, pgId);
      doc.indexDocument.set(FullTextPageConfig.TEXT, text);
      doc.indexDocument.set(FullTextPageConfig.NUMBER, Integer.valueOf(seqNo));
      doc.indexDocument.set(FullTextPageConfig.SEQUENCE, Integer.toString(seqNo));

      PageSearchProxy proxy = new PageSearchProxy();
      proxy.id = pgId;
      proxy.pageNumber = Integer.toString(seqNo);
      proxy.pageSequence = Integer.toString(seqNo);
      proxy.pageText = text;

      doc.proxy = proxy;
      return doc;
   }
}
