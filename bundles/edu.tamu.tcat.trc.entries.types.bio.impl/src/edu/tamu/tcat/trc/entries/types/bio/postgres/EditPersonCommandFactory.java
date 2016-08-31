package edu.tamu.tcat.trc.entries.types.bio.postgres;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.types.bio.postgres.DataModelV1.HistoricalEvent;
import edu.tamu.tcat.trc.entries.types.bio.postgres.DataModelV1.Person;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditPersonCommand;
import edu.tamu.tcat.trc.entries.types.bio.repo.HistoricalEventMutator;
import edu.tamu.tcat.trc.entries.types.bio.repo.PersonNameMutator;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.UpdateContext;

public class EditPersonCommandFactory implements EditCommandFactory<Person, EditPersonCommand>
{

   @Override
   public EditPersonCommand create(String id, UpdateStrategy<Person> strategy)
   {
      return new EditPersonCommandImpl(id, strategy);
   }

   @Override
   public EditPersonCommand edit(String id, UpdateStrategy<Person> strategy)
   {
      return new EditPersonCommandImpl(id, strategy);
   }
   
   private class EditPersonCommandImpl implements EditPersonCommand
   {
      private String id;
      private UpdateStrategy<Person> exec;
      private ApplicableChangeSet<Person> changes = new BasicChangeSet<>();

      public EditPersonCommandImpl(String id, UpdateStrategy<Person> strategy)
      {
         this.id = id;
         this.exec = strategy;
      }

      @Override
      public PersonNameMutator addName()
      {
         changes.add("Add canonical name", person -> {
            person.displayName = new DataModelV1.PersonName();
         });
         
         ChangeSet<DataModelV1.PersonName> person = changes.partial("Add person name", p -> {
            return p.displayName;
         });
         
         return new PersonNameMutatorImpl(person);
      }

      @Override
      public PersonNameMutator addNametoList()
      {
         changes.add("Add name to list", person -> {
            if (person.names == null)
               person.names = new ArrayList<>();
            
            DataModelV1.PersonName name = new DataModelV1.PersonName();
            name.displayName = "";
            person.names.add(name);
         });
         

         ChangeSet<DataModelV1.PersonName> person = changes.partial("Add alternate person name", p -> {
            
            for (DataModelV1.PersonName pn : p.names)
            {
               if (pn.displayName == "")
                  return pn;
            }
            
            return null;
         });
         
         return new PersonNameMutatorImpl(person);
      }

      @Override
      public void clearNameList()
      {
         changes.add("Clear alt names", person -> {
            person.names = null;
         });
      }

      
      @Override
      public PersonNameMutator editName()
      {

         ChangeSet<DataModelV1.PersonName> person = changes.partial("Edit person name", p -> {
            return p.displayName;
         });
         
         return new PersonNameMutatorImpl(person);
      }

      @Override
      public HistoricalEventMutator addBirthEvt()
      {
         changes.add("Add birth event", person -> {
            person.birth = new HistoricalEvent();
         });
         
         ChangeSet<HistoricalEvent> partial = changes.partial("Add birth event", (person) -> {
            return person.birth;
         });
         return new HistoricalEventMutatorImpl(partial);
      }

      @Override
      public HistoricalEventMutator editBirthEvt()
      {
         ChangeSet<HistoricalEvent> birthEvent = changes.partial("Edit birth event", (person) -> {
            return person.birth;
         });
         return new HistoricalEventMutatorImpl(birthEvent);
      }

      @Override
      public HistoricalEventMutator addDeathEvt()
      {
         changes.add("Add death event", person -> {
            person.death = new HistoricalEvent();
         });
         
         ChangeSet<HistoricalEvent> partial = changes.partial("Add death event", (person) -> {
            return person.death;
         });
         return new HistoricalEventMutatorImpl(partial);
      }

      @Override
      public HistoricalEventMutator editDeathEvt()
      {
         ChangeSet<HistoricalEvent> deathEvent = changes.partial("Edit death event", (person) -> {
            return person.death;
         });
         return new HistoricalEventMutatorImpl(deathEvent);
      }

      @Override
      public void setSummary(String summary)
      {
         changes.add("Set summary", person -> person.summary = summary);
      }

      @Override
      public Future<String> execute()
      {
         CompletableFuture<Person> modified = exec.update(ctx -> {
            Person dto = preModifiedData(ctx);
            return this.changes.apply(dto);
         });
         
         return modified.thenApply(dto -> dto.id);
      }
      
      private Person preModifiedData(UpdateContext<Person> ctx)
      {
         Person dto = null;
         Person orig = ctx.getOriginal();
         
         if (orig == null)
         {
            dto = new Person();
            dto.id = this.id;
         }
         else
            dto = Person.copy(orig);
         return dto;
      }
   }

}
