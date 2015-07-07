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

import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;
import edu.tamu.tcat.trc.entries.types.bio.Person;

/**
 * The bibliographic name of the creator or other contributor to a work along with the role
 * that person played (such as Author, Editor or Translator). This should be represented as
 * the person's name appears on the title page or other authorial attribution section of the
 * work itself.
 *
 * <p>
 * The {@code AuthorReference} includes an identifier that allows the bibliographic name to be
 * associated with a biographical entry for the person represented by this author. This allows the
 * bibliographic record to capture the name of the author as it appears on the work, along with
 * the role the author played in the creation of this work (e.g., author, translator, editor,
 * director, etc).
 *
 * @see WorkRepository#getAuthor(AuthorReference) To retrieve the {@link Person} associated
 * with an {@code AuthorReference}.
 */
public interface AuthorReference
{
   /**
    * @return The unique identifier of the referenced author.
    */
   String getId();

   /**
    * @return The name of the author as it appears on the work.
    * @deprecated Use {@link #getLastName()} and {@link #getFirstName()} instead.
    */
   @Deprecated
   String getName();

   /**
    * @return The first portion of the author's name (including, optionally, middle names).
    *       Many citation guidelines specify different formatting for the first and last
    *       portion of the author's name. This field is used to facilitate such formatting.
    */
   String getFirstName();

   /**
    * @return The last portion of the author's name. Many citation guidelines specify
    *       different formatting for the first and last portion of the author's name.
    *       This field is used to facilitate such formatting. In particular, note that
    *       this field is intended to be used for lexicographic ordering of bibliographic
    *       entries by author's name.
    */
   String getLastName();

   /**
    * @return The role this person played in the creation of the work, for example, author,
    *    translator, editor, director, etc.. This is an application specific value.
    */
   String getRole();


}
