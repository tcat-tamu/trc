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

/**
 * Provides a structured representation of a title.
 */
public interface Title
{
   /**
    * Standard title type to indicate a title commonly used to reference a work
    * as shorthand when the use of the full title is not warrented. For example,
    * Tractatus for Spinoza's Treatise Theological and Political.

    */
   public static final String SHORT = "short";
   public static final String CANONICAL = "canonical";
   public static final String BIBLIOGRAHIC = "bibliographic";

   /**
    * @return The type of title: canonical, short, etc...
    */
   String getType();

   /**
    * @return The main title of a work that can be sub-divided into a title and sub-title. Will
    *    not be {@code null}. If the title is not structured, this will have the same value as
    *    {@link #getFullTitle()}.
    */
   String getTitle();

   /**
    * @return The sub-title of a work that can be sub-divided into a title and sub-title. Will
    *    not be {@code null} but may be empty if no sub-title is defined for this work.
    */
   String getSubTitle();

   /**
    * @return A full representation of the title of this work as it appears on the title-page
    *    of the work or some other canonical representation. This may be inferred from the
    *    structured form (e.g., 'Title: Subtitle') or supplied explicitly.
    */
   String getFullTitle();

   /**
    * @return The language that the title is written in.
    */
   String getLanguage();
}
