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
package edu.tamu.tcat.trc.entries.types.biblio.rest.v1.copies;

import java.util.Map;

/**
 * An encapsulation of the data vehicle types used to process JSON requests and responses
 * for version 1 of the TRC REST API for Bibliographic entries.
 */
public class RestApiV1
{
   public static class CopyReferenceId
   {
      public String id;
   }

   public static class CopyReference
   {
      public String id;
      public String type;
      public Map<String, String> properties;
      public String title;
      public String summary;
      public String rights;
   }
}
