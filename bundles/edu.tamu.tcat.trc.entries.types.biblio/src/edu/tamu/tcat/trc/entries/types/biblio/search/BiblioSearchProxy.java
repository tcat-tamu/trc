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
package edu.tamu.tcat.trc.entries.types.biblio.search;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON serializable summary information about a work. Intended to be
 * returned when only a brief summary of the work is required to save
 * data transfer and parsing resources.
 *
 */
public class BiblioSearchProxy
{
   public String id;
   public String token;
   public String type;
   public String uri;
   public List<AuthorProxy> authors = new ArrayList<>();
   public String title;
   public String label;
   public String summary;
   public String pubYear = null;

   public static class AuthorProxy
   {
      public String authorId;
      public String firstName;
      public String lastName;
      public String role;
   }
}
