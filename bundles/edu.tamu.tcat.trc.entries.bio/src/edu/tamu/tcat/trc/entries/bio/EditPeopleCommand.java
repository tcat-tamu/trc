package edu.tamu.tcat.trc.entries.bio;

import java.util.Set;
import java.util.concurrent.Future;

import edu.tamu.tcat.catalogentries.events.dv.HistoricalEventDV;
import edu.tamu.tcat.trc.entries.bio.dv.PersonDV;
import edu.tamu.tcat.trc.entries.bio.dv.PersonNameDV;

public interface EditPeopleCommand
{
   /**
    * Sets all properties given the supplied {@link PersonDV}
    *
    * @param person The data vehicle used to update the person.
    */
   void setAll(PersonDV person);

   /**
    * Updates the set of all names given to the person
    * @param names
    */
   void setNames(Set<PersonNameDV> names);

   /**
    * Updates the cannonical name of the person
    * @param personName
    */
   void setName(PersonNameDV personName);

   /**
    * Updates the persons birth as a historical event
    * @param birth
    */
   void setBirthEvt(HistoricalEventDV birth);

   /**
    * Updates the persons death as a hsitorical event
    * @param death
    */
   void setDeathEvt(HistoricalEventDV death);

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
