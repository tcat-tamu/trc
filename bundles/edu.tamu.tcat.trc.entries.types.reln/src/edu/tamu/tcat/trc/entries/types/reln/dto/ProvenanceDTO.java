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
package edu.tamu.tcat.trc.entries.types.reln.dto;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.reln.Provenance;
import edu.tamu.tcat.trc.entries.types.reln.URIParseHelper;
import edu.tamu.tcat.trc.entries.types.reln.internal.dto.BasicProvenance;

public class ProvenanceDTO
{
   private static final DateTimeFormatter iso8601Formatter = DateTimeFormatter.ISO_INSTANT;

   /** The string-valued URIs associated with the creators of the associated annotation. */
   public Set<String> creatorUris;

   /** Date created in ISO 8601 "instant" format such as '2011-12-03T10:15:30Z' */
   public String dateCreated;

   /** Date modified in ISO 8601 "instant" format such as '2011-12-03T10:15:30Z' */
   public String dateModified;

   public static ProvenanceDTO create(Provenance prov)
   {
      ProvenanceDTO result = new ProvenanceDTO();
      Instant created = prov.getDateCreated();
      result.dateCreated = (created != null) ? iso8601Formatter.format(created) : null;

      Instant modified = prov.getDateModified();
      result.dateModified = (modified != null) ? iso8601Formatter.format(modified) : null;

      result.creatorUris = URIParseHelper.toStringSet(prov.getCreators());

      return result;
   }

   /**
    * Note that if either the creation or modification date of the supplied PovenanceDV is
    * {@code null}, the corresponding property of the resulting {@link Provenance} will be
    * initialized to the current instant.
    *
    * @param data
    * @return
    */
   public static Provenance instantiate(ProvenanceDTO data)
   {
      // TODO handle format errors
      Instant created = (data.dateCreated != null) ? Instant.parse(data.dateCreated) : Instant.now();
      Instant modified = (data.dateModified != null) ? Instant.parse(data.dateModified) : Instant.now();
      Collection<URI> creators = URIParseHelper.parse(data.creatorUris);

      return new BasicProvenance(creators, created, modified);
   }
}
