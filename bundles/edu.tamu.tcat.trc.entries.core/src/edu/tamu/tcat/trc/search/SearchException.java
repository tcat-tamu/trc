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
package edu.tamu.tcat.trc.search;

/**
 * A base exception type for entry types to use within their search modules.
 */
public class SearchException extends Exception
{
   public SearchException()
   {
   }

   public SearchException(String message)
   {
      super(message);
   }

   public SearchException(Throwable cause)
   {
      super(cause);
   }

   public SearchException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public SearchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
