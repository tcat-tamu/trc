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
package edu.tamu.tcat.trc.entries.types.biblio;

import edu.tamu.tcat.trc.entries.common.DateDescription;


/**
 * Publication details for a particular work.
 */
public interface PublicationInfo
{
   /**
    * @return The place where this work was published.
    */
   String getLocation();         // TODO make first class entity

   /**
    * @return The person or organization responsible for publishing this work.
    */
   String getPublisher();        // TODO make first class entity
   
   /**
    * @return The date this work was published.
    */
   DateDescription getPublicationDate();    //  TODO should be HistoricalDate
}
