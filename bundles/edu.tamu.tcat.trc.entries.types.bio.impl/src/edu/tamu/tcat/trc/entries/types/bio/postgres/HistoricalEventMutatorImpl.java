package edu.tamu.tcat.trc.entries.types.bio.postgres;

import java.util.Date;

import edu.tamu.tcat.trc.entries.types.bio.postgres.DataModelV1.DateDescription;
import edu.tamu.tcat.trc.entries.types.bio.postgres.DataModelV1.HistoricalEvent;
import edu.tamu.tcat.trc.entries.types.bio.repo.DateDescriptionMutator;
import edu.tamu.tcat.trc.entries.types.bio.repo.HistoricalEventMutator;
import edu.tamu.tcat.trc.repo.ChangeSet;

public class HistoricalEventMutatorImpl implements HistoricalEventMutator
{
   private ChangeSet<HistoricalEvent> changes;

   public HistoricalEventMutatorImpl(ChangeSet<HistoricalEvent> changes)
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
      changes.add("Set description", event -> event.description = description);
   }

   @Override
   public void setLocations(String location)
   {
      changes.add("Set location", event -> event.location = location);
   }
   
   @Override
   public void setDate(Date eventDate)
   {
      changes.add("Set date", event -> event.eventDate = eventDate);
   }

   @Override
   public DateDescriptionMutator addDateDescription()
   {
      
      changes.add("Add Date Description", event -> {
         event.date = new DateDescription();
      });
      
      ChangeSet<DateDescription> partial = changes.partial("Add date description", (event) -> {
         return event.date;
      });
      
      return new DateDescriptionMutatorImpl(partial);
   }

   @Override
   public DateDescriptionMutator editDateDescription()
   {
      ChangeSet<DateDescription> editDateDescription = changes.partial("Edit date description", (event) -> {
         return event.date;
      });
      
      return new DateDescriptionMutatorImpl(editDateDescription);
   }
   
   private class DateDescriptionMutatorImpl implements DateDescriptionMutator
   {

      private ChangeSet<DateDescription> dateChangeSet;

      public DateDescriptionMutatorImpl(ChangeSet<DateDescription> dateChangeSet)
      {
         this.dateChangeSet = dateChangeSet;
      }
      
      @Override
      public void setCalendar(String calendar)
      {
         dateChangeSet.add("Set calendar", date -> date.calendar = calendar);
      }

      @Override
      public void setDescription(String description)
      {
         dateChangeSet.add("Set description", date -> date.description = description);
      }
   }
}
