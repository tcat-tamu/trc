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

import edu.tamu.tcat.trc.digires.books.discovery.DigitalCopyProxy;

public class HTCopyProxy implements DigitalCopyProxy
{
   public String identifier;
   public String title;
   public String description;
   public String copyProvider = "HathiTrust";
   public String sourceSummary;
   public String rights;
   public String publicationDate;

   public HTCopyProxy()
   {
      // TODO Auto-generated constructor stub
   }

   @Override
   public String getIdentifier()
   {
      return this.identifier;
   }

   @Override
   public String getTitle()
   {
      return this.title;
   }

   @Override
   public String getDescription()
   {
      return this.description;
   }

   @Override
   public String getCopyProvider()
   {
      return this.copyProvider;
   }

   @Override
   public String getSourceSummary()
   {
      return this.sourceSummary;
   }

   @Override
   public String getRights()
   {
      return this.rights;
   }

   @Override
   public String getPublicationDate()
   {
      return this.publicationDate;
   }

}
