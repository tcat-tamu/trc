package edu.tamu.tcat.trc.entries.search.solr.impl;

import java.time.Year;
import java.util.Objects;

//TODO: add constructors and fields to support unbounded date ranges
/**
 * A helper data transfer object to be used in search command implementations to
 * manage a date range.
 */
public class DateRangeDTO
{
   public final Year start;
   public final Year end;

   public DateRangeDTO(Year s, Year e)
   {
      start = Objects.requireNonNull(s, "Start date may not be null");
      end = Objects.requireNonNull(e, "End date may not be null");
   }
}
