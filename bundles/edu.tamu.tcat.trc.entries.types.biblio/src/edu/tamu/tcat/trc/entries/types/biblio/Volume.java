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

import java.util.Collection;
import java.util.List;

/**
 * A physical volume in which an edition is published. Editions of a work are published in some media, typically one
 * or more volumes. While the bibliographic description of the parent work is typically inherited by different volumes,
 * individual volumes may need to supply their own values for these fields. For example, the 12 volumes of the
 * 1910-1915 edition of <em>The Fundamentals</em> have different publication dates and editors.
 *
 * <dl>
 *   <dt>Volume Title</dt><dd>If applicable, the title of the volume</dd>
 *   <dt>Volume</dt><dd>The number or other authoritative identifier for this volume</dd>
 * </dl>
 *
 */
public interface Volume
{
   /**
    * @return A unique system identifier for this volume.
    */
   String getId();

   /**
    * The number or authoritative identifier for this volume.
    *
    * @return
    */
   String getVolumeNumber();

   /**
    * Volumes have publication information that is usually the same as that of their edition, but
    * it could potentially differ. This value will not be null.
    *
    * @return
    */
   PublicationInfo getPublicationInfo();

   /**
    * Volumes have their own series of authors who may not contribute to the underlying {@link
    * Edition} or {@link Work}. This list should be disjoint with the list of authors of the
    * underlying Edition and Work.
    *
    * @return
    */
   List<AuthorReference> getAuthors();

   /**
    * Titles of volumes may vary independently from the original work title.
    *
    * @return
    */
   Collection<Title> getTitles();

   /**
    * Other individuals who played a role in the creation of this work, but who are not primarily
    * responsible for its creation. Translators are a common example.
    *
    * @return The other authors associated with this work.
    */
   List<AuthorReference> getOtherAuthors();

   /**
    * Series to which the works belongs
    *
    * @return
    */
   String getSeries();

   /**
    * A descriptive summary about this volume.
    *
    * @return
    */
   String getSummary();

}
