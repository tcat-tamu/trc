package edu.tamu.tcat.trc.entries.types.bio.impl.repo;

import static java.text.MessageFormat.format;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.trc.entries.types.bio.repo.DateDescriptionMutator;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditPersonCommand;
import edu.tamu.tcat.trc.entries.types.bio.repo.HistoricalEventMutator;
import edu.tamu.tcat.trc.entries.types.bio.repo.PersonNameMutator;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.UpdateContext;

public class EditPersonCommandFactory implements EditCommandFactory<DataModelV1.Person, EditPersonCommand>
{

   @Override
   public EditPersonCommand create(String id, UpdateStrategy<DataModelV1.Person> strategy)
   {
      return new EditPersonCommandImpl(id, strategy);
   }

   @Override
   public EditPersonCommand edit(String id, UpdateStrategy<DataModelV1.Person> strategy)
   {
      return new EditPersonCommandImpl(id, strategy);
   }

   private class EditPersonCommandImpl implements EditPersonCommand
   {
      private String id;
      private UpdateStrategy<DataModelV1.Person> exec;
      private ApplicableChangeSet<DataModelV1.Person> changes = new BasicChangeSet<>();

      public EditPersonCommandImpl(String id, UpdateStrategy<DataModelV1.Person> strategy)
      {
         this.id = id;
         this.exec = strategy;
      }

      @Override
      public PersonNameMutator editCanonicalName()
      {

         ChangeSet<DataModelV1.PersonName> partial = changes.partial("displayName [Edit]", p -> {
            if (p.displayName == null)
               p.displayName = new DataModelV1.PersonName();

            return p.displayName;
         });

         return new PersonNameMutatorImpl(partial);
      }

      @Override
      public PersonNameMutator addAlternateName()
      {
         String pId = UUID.randomUUID().toString();
         String msg = format("names.{1} [Add]", pId);
         changes.add(msg, person -> {
            if (person.names == null)
               person.names = new ArrayList<>();

            DataModelV1.PersonName name = new DataModelV1.PersonName();
            name.id = pId;
            person.names.add(name);
         });

         String editMsg = format("names.{1} [Edit]", pId);
         ChangeSet<DataModelV1.PersonName> partial = changes.partial(editMsg, p -> {
            return p.names.stream().filter(n -> n.id.equals(pId)).findFirst().get();
         });

         return new PersonNameMutatorImpl(partial);
      }

      @Override
      public void clearAlternateNames()
      {
         changes.add("names [Clear]", person -> {
            person.names = new ArrayList<>();
         });
      }

      @Override
      public HistoricalEventMutator editBirth()
      {
         ChangeSet<DataModelV1.HistoricalEvent> partial = changes.partial("birth [Edit]", (person) -> {
            if (person.birth == null)
               person.birth = new DataModelV1.HistoricalEvent();

            return person.birth;
         });
         return new HistoricalEventMutatorImpl(partial);
      }

      @Override
      public HistoricalEventMutator editDeath()
      {
         ChangeSet<DataModelV1.HistoricalEvent> partial = changes.partial("death [Edit]", (person) -> {
            if (person.death == null)
               person.death = new DataModelV1.HistoricalEvent();

            return person.death;
         });

         return new HistoricalEventMutatorImpl(partial);
      }

      @Override
      public void setSummary(String summary)
      {
         changes.add("summary", person -> person.summary = summary);
      }

      @Override
      public CompletableFuture<String> execute()
      {
         CompletableFuture<DataModelV1.Person> modified = exec.update(ctx -> {
            DataModelV1.Person dto = preModifiedData(ctx);
            return this.changes.apply(dto);
         });

         return modified.thenApply(dto -> dto.id);
      }

      private DataModelV1.Person preModifiedData(UpdateContext<DataModelV1.Person> ctx)
      {
         DataModelV1.Person orig = ctx.getOriginal();

         DataModelV1.Person dto = null;
         if (orig == null)
         {
            dto = new DataModelV1.Person();
            dto.id = this.id;
         }
         else
         {
            dto = DataModelV1.Person.copy(orig);
         }
         return dto;
      }
   }

   public static class PersonNameMutatorImpl implements PersonNameMutator
   {
      private final ChangeSet<DataModelV1.PersonName> changes;

      public PersonNameMutatorImpl(ChangeSet<DataModelV1.PersonName> partial)
      {
         this.changes = partial;
      }

      @Override
      public void setTitle(String title)
      {
         changes.add("Set title", name -> name.title = title);
      }

      @Override
      public void setGivenName(String first)
      {
         changes.add("Set given", name -> name.givenName = first);
      }

      @Override
      public void setMiddleName(String middle)
      {
         changes.add("Set middle", name -> name.middleName = middle);
      }

      @Override
      public void setFamilyName(String family)
      {
         changes.add("Set family", name -> name.familyName = family);
      }

      @Override
      public void setSuffix(String suffix)
      {
         changes.add("Set suffix", name -> name.suffix = suffix);
      }

      @Override
      public void setDisplayName(String displayName)
      {
         changes.add("Set Display Name", name -> name.displayName = displayName);
      }
   }

   public static class HistoricalEventMutatorImpl implements HistoricalEventMutator
   {
      private ChangeSet<DataModelV1.HistoricalEvent> changes;

      public HistoricalEventMutatorImpl(ChangeSet<DataModelV1.HistoricalEvent> changes)
      {
         this.changes = changes;
      }

      @Override
      public void setTitle(String title)
      {
         changes.add("Set title", event -> event.title = title);
      }

      @Override
      public void setDescription(String description)
      {
         changes.add("description", event -> event.description = description);
      }

      @Override
      public void setLocation(String location)
      {
         changes.add("location", event -> event.location = location);
      }


      @Override
      public DateDescriptionMutator editDate()
      {
         ChangeSet<DataModelV1.DateDescription> partial = changes.partial("date", (event) -> {
            if (event.date == null)
               event.date = new DataModelV1.DateDescription();
            return event.date;
         });

         return new DateDescriptionMutatorImpl(partial);
      }

      private class DateDescriptionMutatorImpl implements DateDescriptionMutator
      {

         private ChangeSet<DataModelV1.DateDescription> dateChangeSet;

         public DateDescriptionMutatorImpl(ChangeSet<DataModelV1.DateDescription> dateChangeSet)
         {
            this.dateChangeSet = dateChangeSet;
         }

         @Override
         public void setDescription(String description)
         {
            dateChangeSet.add("description", date -> date.description = description);
         }

         @Override
         public void setCalendar(LocalDate calendar)
         {
            dateChangeSet.add("calendar",
                  date -> date.calendar = DateTimeFormatter.ISO_LOCAL_DATE.format(calendar));
         }
      }
   }
}