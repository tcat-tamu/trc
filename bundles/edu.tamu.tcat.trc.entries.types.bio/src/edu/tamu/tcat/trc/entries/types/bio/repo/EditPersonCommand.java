package edu.tamu.tcat.trc.entries.types.bio.repo;

import java.util.Set;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.common.dto.HistoricalEventDTO;
import edu.tamu.tcat.trc.entries.types.bio.Person;
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
