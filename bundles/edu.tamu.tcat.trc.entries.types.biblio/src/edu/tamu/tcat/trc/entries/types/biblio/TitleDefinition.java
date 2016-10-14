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
package edu.tamu.tcat.trc.entries.types.biblio;

import java.util.Optional;
import java.util.Set;

/**
 * The title of a work. In practice, documenting the title of a historical work can be a
 * complex task. The title as it appears on the title page may be exceptionally long (in
 * excess of one hundred words) and multiple representations of the title may be used in
 * different places for different purposes. Consequently, we need to represent different
 * versions of a title for a single work and support certain common use cases.
 *
 * <p>For example, the book <em>Tractatus Theologico-Politicus</em>, is commonly refered to
 * by its shorter designation <em>Tractatus</em> and sometimes using the English translation
 * <em>Theologico-Political Treatise</em>. The full title is rather longer, as represented in
 * this translation: <em>Tractatus theologico-politicus: a critical inquiry into the history,
 * purpose, and authenticity of the Hebrew scriptures; with the right to free thought and free
 * discussion asserted, and shown to be not only consistent but necessarily bound up with
 * true piety and good government.</em>
 *
 */
public interface TitleDefinition
{
   // FIXME this should provide a more generic API.
   //       access by role.

   /**
    * @return A set of all titles defined for this work
    */
   Set<Title> get();

   /**
    * @param type The title type to retrieve
    * @return An optional containing the requested title.
    */
   Optional<Title> get(String type);

   /**
    * @return A set containing all defined title types.
    */
   Set<String> getTypes();
}
