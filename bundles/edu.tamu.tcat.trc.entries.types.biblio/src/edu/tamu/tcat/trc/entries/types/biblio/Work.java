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

import java.util.List;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;


/**
 * Bibliographic description for a book, article, journal or other work. This is the main
 * point of entry for working with bibliographic records.
 */
public interface Work
{
   // TODO rename to BibliographicEntry

   /**
    * @return A unique, persistent identifier for this work.
    */
   String getId();      // TODO create named ID type

   /**
    * @return The authors of this work.
    */
   AuthorList getAuthors();

   /**
    * @return The title of this work.
    */
   TitleDefinition getTitle();

   /**
    * @return Secondary authors associated with this work. This corresponds to authors that
    *    would typically be displayed after the title information, such as the translator of a
    *    work. For example, in the entry Spinoza. <em>Tractatus Theologico-Politicus</em>.
    *    Trans by Willis. 1862. Willis would be the 'outher authors'.
    *
    */
   AuthorList getOtherAuthors();

   /**
    * @return The editions associated with this work sorted by publication date in ascending order.
    */
   List<Edition> getEditions();

   /**
    * Obtain a particular edition of this work by edition ID.
    *
    * @param editionId
    * @return The edition associated with this work that possesses the given ID or {@code null} if
    *       an edition with the given ID cannot be found.
    */
   Edition getEdition(String editionId);

   /**
    * @return A defined series of related works, typically published by a single publishers and
    *    issued under the direction of a series editor or editors.
    */
   String getSeries();                    // TODO make series a first-level entity

   /**
    * @return A brief summary of this work.
    */
   String getSummary();

   /**
    * @return The default copy reference associated with this work or {@code null} if no default has
    *       been set.
    */
   CopyReference getDefaultCopyReference();

   /**
    * @return all copy references that have been affiliated with this work.
    */
   Set<CopyReference> getCopyReferences();
}
