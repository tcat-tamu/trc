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
package edu.tamu.tcat.trc.entries.core.repo;

import edu.tamu.tcat.trc.ResourceNotFoundException;

/**
 * Indicates that a TRC entry (such as a bibliographic entry or biographical
 * details) does not exist. Typically thrown when attempting to access an entry by
 * a unique identifier.
 */
public class NoSuchEntryException extends ResourceNotFoundException
{
   // FIXME -- never actually instantiated
   // FIXME -- rename to EntryNotFoundException
   // FIXME should clarify the exception hierarchy to ensure that this exception is not
   //       propagated via the entry repo API
   public NoSuchEntryException()
   {
   }

   public NoSuchEntryException(String message)
   {
      super(message);
   }

   public NoSuchEntryException(Throwable cause)
   {
      super(cause);
   }

   public NoSuchEntryException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public NoSuchEntryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
