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
package edu.tamu.tcat.trc.entries.common;


/**
 * Provides a representation of a historical event that occurs as a specific
 * place and date.
 */
public interface HistoricalEvent
{

   // NOTE This will be extended and revised significantly as we flesh out the notion
   //      of events. Should perhaps be changed to be a more simple identifier and we can
   //      use other controls to attach additional info, but I think, a start date, end date,
   //      location, title and description are probably a good basic description for the

   /**
    * @return A unique, persistent identifier for this event.
    */
   String getId();

   /**
    * @return A title for this event for display purposes.
    */
   String getTitle();

   /**
    * @return A brief description of this event.
    */
   String getDescription();

   /**
    * @return The date at when this event happened. This value will not be null.
    */
   DateDescription getDate();

   /**
    * @return The location where this event happened.
    */
   String getLocation();
}
