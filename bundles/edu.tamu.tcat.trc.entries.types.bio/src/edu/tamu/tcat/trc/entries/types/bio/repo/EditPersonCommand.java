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

import java.util.Set;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.dto.HistoricalEventDTO;
import edu.tamu.tcat.trc.entries.types.bio.dto.PersonDTO;
import edu.tamu.tcat.trc.entries.types.bio.dto.PersonNameDTO;

/**
 * A command allowing editing of new or existing {@link Person} entries in a
 * {@link PeopleRepository}.
 */
public interface EditPersonCommand
{
   /**
    * Sets all properties given the supplied {@link PersonDTO}
    *
    * @param person The data vehicle used to update the person.
    */
   void setAll(PersonDTO person);

   /**
    * Updates the set of all names given to the person
    * @param names
    */
   void setNames(Set<PersonNameDTO> names);

   /**
    * Updates the cannonical name of the person
    * @param personName
    */
   void setName(PersonNameDTO personName);

   /**
    * Updates the persons birth as a historical event
    * @param birth
    */
   void setBirthEvt(HistoricalEventDTO birth);

   /**
    * Updates the persons death as a hsitorical event
    * @param death
    */
   void setDeathEvt(HistoricalEventDTO death);

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
