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
package edu.tamu.tcat.trc.entries.types.reln;

import java.util.Collection;

/**
 *  Represents a relationship of some type between two or more entities within the TRC
 *  Entries framework. Relationships are used to provide editorial or scholarly commentary
 *  on the intellectual connections between related items. For example, a relationship might
 *  be used to indicate that a particular book was written as a refutation of an argument
 *  presented in another book, to describe a significant connection between two people or
 *  to document the impact that a particular person had within an intellectual movement.
 *
 *  <p>This differs from the core TRC SeeAlso Service in that relationships are intended to
 *  offer a significant editorial or scholarly contribution whereas SeeAlso links document
 *  related resources that may be useful for consultation. SeeAlso links are considered to be
 *  part of the Entries they decorate, whereas relationships are intended to be a significant
 *  contribution in their own right that may be further supported by narrative description,
 *  bibliographic references, and other TRC Services. Relationships, like other TRC Entries
 *  are authored and versioned.
 *
 *  <p>Commonly, relationships will be ordered pairs (for example, one work was influenced by
 *  another) but may represent more complex structure (such as noting that four works
 *  developed a similar idea in slightly different ways without any clear dependence on each
 *  other). The specific interpretation of the related entries will be governed by the
 *  {@link RelationshipType} of this {@link Relationship}.
 *
 *  <p>Relationships may be directed or undirected. A directed relationship will have a
 *  non-empty destination {@link AnchorSet}. Directionality should be understood to indicate
 *  that one entry (or set of entries) is related as described by the {@link RelationshipType}
 *  to another entry (or set of entries) and implies that the source entry is somehow about
 *  the destination entry. An undirected relationship indicates merely that, in the judgment of
 *  the relationship creator, two or more entries are related without implying 'aboutness' or
 *  intentionality (see {@link http://plato.stanford.edu/entries/intentionality/}) among the
 *  related entries.
 *
 *  <p>Relationships reference {@link Anchor}s rather than catalog entries directly. Anchors
 *  support additional metadata that can be used in application specific ways to annotate the
 *  endpoint, for example, to reference internal structure such as a page range within an
 *  edition or a digital copy of a book, a span of text, a region within an image, etc.
 */
public interface Relationship
{
   /**
    * @return The unique identifier for this relationship.
    */
   String getId();

   /**
    * @return The type of this relationship. All relationships are typed. The specific
    *    relationship types that are available is determined by the configuration and
    *    deployment of a particular application to suite the editorial needs of the
    *    project. The relationship types typically define (either implicitly or explicitly)
    *    the types of entities that may be related, whether or not relationships are
    *    directed and various other properties of the relationship that are used to
    *    control its display in user interfaces, influence its the interpretation in
    *    automated reasoning systems, and support refined querying and filtering.
    */
   RelationshipType getType();

   /**
    * @return A narrative description of this relationship. This description may
    *       contain light HTML markup.
    */
   String getDescription();

   /**
    * @return A collection of {@link Anchor}s to the entries described by this relationship.
    */
   Collection<Anchor> getRelatedEntities();

   /**
    *  @return For directed relationships, returns the entities that are being referenced.
    *    For undirected relationships, returns an empty collection.
    */
   Collection<Anchor> getTargetEntities();
}
