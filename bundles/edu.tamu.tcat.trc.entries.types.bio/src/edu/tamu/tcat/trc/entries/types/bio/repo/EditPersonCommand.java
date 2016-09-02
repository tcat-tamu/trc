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
package edu.tamu.tcat.trc.entries.types.bio.repo;

import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.types.bio.Person;

/**
 * A command allowing editing of new or existing {@link Person} entries in a
 * {@link PeopleRepository}.
 */
public interface EditPersonCommand
{
//   /**
//    * Sets all properties given the supplied {@link PersonDTO}
//    *
//    * @param person The data vehicle used to update the person.
//    */
//   @Deprecated
//   void setAll(PersonDTO person);
//
//   /**
//    * Updates the set of all names given to the person
//    * @param names
//    */
//   @Deprecated
//   void setNames(Set<PersonNameDTO> names);

   /**
    * Creates a mutator to add a name for the person.
    * @return
    */
   PersonNameMutator addName();
   
   /**
    * Updates the cannonical name of the person
    * @return PersonNameMutator
    */
   PersonNameMutator editName();

   /**
    * Updates the alternate names for the person.
    * @return 
    */
   PersonNameMutator addNametoList();
   
   /**
    * Clears all alternate names in the list
    */
   void clearNameList();
   
   /**
    * Creates a birth Historical Event for the person
    * @return
    */
   HistoricalEventMutator addBirthEvt();
   
   /**
    * Updates the persons birth as a historical event
    * @param birth
    */
   HistoricalEventMutator editBirthEvt();
   
   /**
    * Creates a death Historical Event for the person
    * @return
    */
   HistoricalEventMutator addDeathEvt();
   
   /**
    * Updates the persons death as a hsitorical event
    * @param death
    */
   HistoricalEventMutator editDeathEvt();

   /**
    * Updates the provided description of the person.
    * @param summary
    */
   void setSummary(String summary);

   /**
    *
    * @return The id of the person that has been created or updated.
    */
   Future<String> execute();
}
