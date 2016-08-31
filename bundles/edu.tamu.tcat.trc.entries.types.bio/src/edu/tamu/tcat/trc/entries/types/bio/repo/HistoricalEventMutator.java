package edu.tamu.tcat.trc.entries.types.bio.repo;

import java.util.Date;

public interface HistoricalEventMutator
{
   void setTitle(String title);
   void setDescription(String description);
   void setLocations(String location);
   DateDescriptionMutator addDateDescription();
   DateDescriptionMutator editDateDescription();
   void setDate(Date eventDate); // what was the purpose of this date?
}
