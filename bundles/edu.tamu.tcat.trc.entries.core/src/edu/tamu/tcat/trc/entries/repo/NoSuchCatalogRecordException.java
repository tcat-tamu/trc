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

/**
 * Indicates that a catalog record (such as a bibliographic entry or biographical
 * details) does not exist. Typically thrown when attempting to access an entry by
 * a unique identifier.
 */
public class NoSuchCatalogRecordException extends Exception
{
   // TODO rename
   public NoSuchCatalogRecordException()
   {
   }

   public NoSuchCatalogRecordException(String message)
   {
      super(message);
   }

   public NoSuchCatalogRecordException(Throwable cause)
   {
      super(cause);
   }

   public NoSuchCatalogRecordException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public NoSuchCatalogRecordException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
