package edu.tamu.tcat.trc.entries.types.bio.postgres;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import edu.tamu.tcat.trc.entries.types.bio.repo.DateDescriptionMutator;
import edu.tamu.tcat.trc.entries.types.bio.repo.HistoricalEventMutator;
import edu.tamu.tcat.trc.repo.ChangeSet;

public class HistoricalEventMutatorImpl implements HistoricalEventMutator
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
