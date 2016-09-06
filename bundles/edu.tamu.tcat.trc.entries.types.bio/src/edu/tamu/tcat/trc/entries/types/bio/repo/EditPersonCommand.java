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

import edu.tamu.tcat.trc.entries.common.HistoricalEvent;
import edu.tamu.tcat.trc.entries.core.repo.EditEntryCommand;
import edu.tamu.tcat.trc.entries.types.bio.Person;

/**
 * A command to edit a {@link Person} entries in a {@link PeopleRepository}.
 */
public interface EditPersonCommand extends EditEntryCommand<Person>
{

   /**
    * Edit the canonical name for this person.
    *
    * @return A mutator for use in editing this person's name.
    */
   PersonNameMutator editCanonicalName();

   /**
    * Adds a new alternate name to the list of names associated with a this person. Note
    * that unlike typical mutators, the name will be added immediately and any updates
    * applied to the new name. Consequently, the returned mutator cannot simply be discarded
    * without affecting changes on the person being edited.
    *
    * <p>Note that this can be used either to add a single name to a person entry or
    * used in conjunction with {@link #clearAlternateNames()} to reset the set of alternate
    * names and re-add each name (including changes).
    *
    * @return A mutator for use editing the newly created name.
    */
   PersonNameMutator addAlternateName();

   /**
    * Clears all alternate names. Intended to be used in conjunction with
    * {@link #addAlternateName()} to update all alternate names.
    */
   void clearAlternateNames();

   /**
    * Updates the persons birth as a historical event
    * @param birth
    */
   @Deprecated // Use #setBirth when implemented
   HistoricalEventMutator editBirth();

   /**
    * Updates the persons death as a historical event
    * @param death
    */
   @Deprecated // Use #setDeath when implemented
   HistoricalEventMutator editDeath();

   /**
    * Not yet implemented. HistoricalEvents will be implemented as their own TRC Entry
    * which means they will need to be modified via their own command system. Consequently
    * they will need to be set or linked through some mechanism other than a mutator for
    * an internal data type. The specific design requires additional thought.
    *
    * @param birth The event representing this person's birth date.
    */
   default void setBirth(HistoricalEvent birth)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Not yet implemented. HistoricalEvents will be implemented as their own TRC Entry
    * which means they will need to be modified via their own command system. Consequently
    * they will need to be set or linked through some mechanism other than a mutator for
    * an internal data type. The specific design requires additional thought.
    *
    * @param birth The event representing this person's birth date.
    */
   default void setDeath(HistoricalEvent birth)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * @param summary The new summary for this person.
    */
   void setSummary(String summary);
}
