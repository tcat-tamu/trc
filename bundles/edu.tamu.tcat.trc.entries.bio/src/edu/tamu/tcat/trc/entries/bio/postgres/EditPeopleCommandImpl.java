package edu.tamu.tcat.trc.entries.bio.postgres;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Function;

import edu.tamu.tcat.catalogentries.events.dv.HistoricalEventDV;
import edu.tamu.tcat.trc.entries.bio.EditPeopleCommand;
import edu.tamu.tcat.trc.entries.bio.dv.PersonDV;
import edu.tamu.tcat.trc.entries.bio.dv.PersonNameDV;

public class EditPeopleCommandImpl implements EditPeopleCommand
{

   private final PersonDV person;

   private Function<PersonDV, Future<String>> commitHook;

   EditPeopleCommandImpl(PersonDV person)
   {
      this.person = person;
   }

   public void setCommitHook(Function<PersonDV, Future<String>> hook)
   {
      commitHook = hook;
   }

   @Override
   public void setAll(PersonDV person)
   {
      setName(person.displayName);
      setNames(person.names);
      setBirthEvt(person.birth);
      setDeathEvt(person.death);
      setSummary(person.summary);
   }

   @Override
   public void setNames(Set<PersonNameDV> names)
   {
      person.names = names;
   }

   @Override
   public void setName(PersonNameDV personName)
   {
      person.displayName = personName;
   }

   @Override
   public void setBirthEvt(HistoricalEventDV birth)
   {
      person.birth = birth;
   }

   @Override
   public void setDeathEvt(HistoricalEventDV death)
   {
      person.death = death;
   }

   @Override
   public void setSummary(String summary)
   {
      person.summary = summary;
   }

   @Override
   public Future<String> execute()
   {
      Objects.requireNonNull(commitHook, "");

      return commitHook.apply(person);
   }

}
