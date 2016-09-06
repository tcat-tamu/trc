package edu.tamu.tcat.trc.entries.types.bio.repo;

import java.time.LocalDate;

/**
 * Used to update a DateDescription.
 *
 * @deprecated To be migrated to the HistoricalEvent TRC Entry.
 */
@Deprecated
public interface DateDescriptionMutator
{
   /**
    * @param description The (brief) textual description of this date.
    */
   void setDescription(String description);

   /**
    * @param calendar The calendar date associated with this date description.
    */
   void setCalendar(LocalDate calendar);
}
