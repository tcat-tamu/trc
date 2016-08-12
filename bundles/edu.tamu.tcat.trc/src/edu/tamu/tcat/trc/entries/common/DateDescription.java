/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.entries.common;

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
