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
package edu.tamu.tcat.trc.entries.types.biblio.dto;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.TitleDefinition;

public class TitleDefinitionImpl implements TitleDefinition
{
   private Set<Title> titles = new HashSet<>();

   public TitleDefinitionImpl(Collection<TitleDV> titles)
   {
      this.titles = titles.stream().map(TitleDV::instantiate).collect(Collectors.toSet());
   }

   @Override
   public Title getCanonicalTitle()
   {
      return titles.stream()
                   .filter(t -> "canonical".equalsIgnoreCase(t.getType()))
                   .findAny()
                   .orElse(titles.stream().findAny().orElse(null));
   }

   @Override
   public Title getShortTitle()
   {
      return titles.stream()
            .filter(t -> "short".equalsIgnoreCase(t.getType()))
            .findAny()
            .orElse(null);
   }

   @Override
   public Set<Title> getAlternateTitles()
   {
      return new HashSet<Title>(titles);
   }

   @Override
   public Title getTitle(Locale language)
   {
      return titles.stream()
            .filter(t -> language.getLanguage().equalsIgnoreCase(t.getLanguage()))
            .findAny()
            .orElse(null);
   }
}
