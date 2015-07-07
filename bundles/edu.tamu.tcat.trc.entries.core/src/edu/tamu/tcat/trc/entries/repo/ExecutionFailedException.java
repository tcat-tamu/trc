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
package edu.tamu.tcat.trc.entries.repo;

import java.sql.SQLException;

/**
 * Represents an internal error the the execution of a request against the underlying
 * repository that cannot be corrected by application behavior.  {@link SQLException} errors
 * are one example of this as the corresponding checked exception typically results from a
 * programming or system configuration error (malformed SQL statement or mismatch with the DB).
 */
public class ExecutionFailedException extends RuntimeException
{

   public ExecutionFailedException()
   {
   }

   public ExecutionFailedException(String message)
   {
      super(message);
   }

   public ExecutionFailedException(Throwable cause)
   {
      super(cause);
   }

   public ExecutionFailedException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public ExecutionFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
