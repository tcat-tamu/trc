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
package edu.tamu.tcat.trc.entries.types.reln.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.tamu.tcat.trc.resolver.EntryIdDto;

/**
 * JSON serializable summary information about a relationship entry.
 * Intended to be returned when only a brief summary is required to save
 * data transfer and parsing resources.
 */
public class RelnSearchProxy
{
   public String id;
   public String token;
   public String typeId;
   public String description;

   public Set<Anchor> related = new HashSet<>();
   public Set<Anchor> targets = new HashSet<>();

   public static class Anchor
   {
      public String label;
      public EntryIdDto ref;
      public Map<String, Set<String>> properties = new HashMap<>();
   }
}
