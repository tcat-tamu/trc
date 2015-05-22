package edu.tamu.tcat.trc.entries.types.bio.postgres;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Function;

import edu.tamu.tcat.catalogentries.events.dv.HistoricalEventDV;
import edu.tamu.tcat.trc.entries.types.bio.dto.PersonDTO;
import edu.tamu.tcat.trc.entries.types.bio.dto.PersonNameDTO;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditPersonCommand;

public class EditPeopleCommandImpl implements EditPersonCommand
{

   private final PersonDTO person;

   private Function<PersonDTO, Future<String>> commitHook;

   EditPeopleCommandImpl(PersonDTO person)
   {
      this.person = person;
   }

   public void setCommitHook(Function<PersonDTO, Future<String>> hook)
   {
      commitHook = hook;
   }

   @Override
   public void setAll(PersonDTO person)
   {
      setName(person.displayName);
      setNames(person.names);
      setBirthEvt(person.birth);
      setDeathEvt(person.death);
      setSummary(person.summary);
   }

   @Override
   public void setNames(Set<PersonNameDTO> names)
   {
      person.names = names;
   }

   @Override
   public void setName(PersonNameDTO personName)
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
