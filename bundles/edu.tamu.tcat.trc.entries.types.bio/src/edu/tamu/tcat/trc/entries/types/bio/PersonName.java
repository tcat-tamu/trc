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
package edu.tamu.tcat.trc.entries.types.bio;

/**
 * A structured representation of a person's name.  
 */
public interface PersonName
{
   /**
    * @return The internal id of this PersonName
    */
   String getId();
   
   /**
    * @return The title of address, such as Dr., Mr., Ms.
    */
   String getTitle();
   
   /**
    * @return The prerson's given or first name.
    */
   String getGivenName();
   
   /**
    * 
    * @return A middle name.
    */
   String getMiddleName();

   /**
    * @return The person's family or last name.
    */
   String getFamilyName();
   
   /**
    * @return A suffix such as Jr. or MD
    */
   String getSuffix();
   
   /**
    * @return The person's full name as it should be commonly displayed.
    */
   String getDisplayName();
   
}
