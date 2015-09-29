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
 * Indicates problems accessing resources within a catalog repository.
 */
@Deprecated  // to be renamed/moved
public class CatalogRepoException extends Exception
{

   public CatalogRepoException()
   {
   }

   public CatalogRepoException(String message)
   {
      super(message);
   }

   public CatalogRepoException(Throwable cause)
   {
      super(cause);
   }

   public CatalogRepoException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public CatalogRepoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
