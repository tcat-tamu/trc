package edu.tamu.tcat.catalogentries.events;

import java.time.LocalDate;

/**
 *  Provides a representation of historical dates in which a human understandable description
 *  of the date such as 'Early 1834' is supplemented with an approximate machine readable
 *  calendar date. This is used to represent the discursive practices common in historical
 *  communication while provide a machine readable interpretation of those dates.
 *
 *  <p>Note that the human readable date is taken to be the authoritative information and the
 *  machine readable calendar date is to be considered an approximation.
 *
 *
 */
public interface DateDescription
{
   /**
    * @return The human-readable representation of this date. This value is to be considered
    *       authoritative. May be an empty string if no user information is supplied.
    */
   String getDescription();

   /**
    * @return A machine interpretable calendar date to be used as an approximate value for
    *       computational purposes. May be null.
    */
   LocalDate getCalendar();
}
