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
package edu.tamu.tcat.trc.entries.types.biblio.impl.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.TitleDefinition;

public class BasicTitleDefinition implements TitleDefinition
{
   private final Map<String, Title> titles = new HashMap<>();

   public BasicTitleDefinition(Collection<Title> titles)
   {
      titles.forEach(title -> this.titles.put(title.getType(), title));
   }

   @Override
   public Set<Title> get()
   {
      return new HashSet<>(titles.values());
   }

   @Override
   public Optional<Title> get(String type)
   {
      Title title = titles.get(type);
      return title == null ? Optional.empty() : Optional.of(title);
   }

   @Override
   public Set<String> getTypes()
   {
      return titles.keySet();
   }
}
