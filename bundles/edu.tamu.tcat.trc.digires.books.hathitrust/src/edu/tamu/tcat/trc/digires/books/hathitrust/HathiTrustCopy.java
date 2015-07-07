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
package edu.tamu.tcat.trc.digires.books.hathitrust;

import java.net.URI;

import edu.tamu.tcat.hathitrust.bibliography.Item;
import edu.tamu.tcat.hathitrust.bibliography.Record;
import edu.tamu.tcat.trc.digires.books.resolve.DigitalCopy;

/**
 *  Represents a digital copy of a book from the HathiTrust digital library.
 */
public class HathiTrustCopy implements DigitalCopy
{
   // FIXME at the moment, we're getting the bib info via bib records rather than item records
   //       That means the titles, dates, etc. returned for this copy may not correspond to
   //       the real values for this copy.
   //  TODO document where to look for the supported data formats

   private final String title;
   private String copyLabel;

   private final String recordId;
   private final String itemId;

   private final URI recordUri;
   private final URI itemUri;

   public HathiTrustCopy(String copyId, Record record, Item item)
   {
      // TODO should we get this from HathiFiles rather than bib API?
      // TODO will build out additional detail as needed by the UI and other system tools

      recordId = record.getId();
      itemId = item.getItemId();

      title = record.getTitles().stream().findFirst().orElse("Unknown.");
      copyLabel = item.getSortKey();

      recordUri = record.getRecordURL();
      itemUri = item.getItemURL();
   }

   public String getRecordId()
   {
      return this.recordId;
   }

   public String getItemId()
   {
      return itemId;
   }

   public String getTitle()
   {
      return this.title +
            copyLabel != null && !copyLabel.trim().isEmpty() ? "(" + copyLabel+ ")" : "";
   }

   public URI getRecordUri()
   {
      return this.recordUri;
   }

   public URI getItemUri()
   {
      return itemUri;
   }
}
