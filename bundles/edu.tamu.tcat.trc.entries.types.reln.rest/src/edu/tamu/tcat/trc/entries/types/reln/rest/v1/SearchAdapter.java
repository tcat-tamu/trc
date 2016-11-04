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
package edu.tamu.tcat.trc.entries.types.reln.rest.v1;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.types.reln.search.RelnSearchProxy;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryIdDto;
import edu.tamu.tcat.trc.resolver.EntryResolver;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

/**
 * An encapsulation of adapter methods to convert between the search API and
 * the {@link RestApiV1} schema DTOs.
 */
public class SearchAdapter
{
   public static List<RestApiV1.RelationshipSearchResult> toDTO(List<RelnSearchProxy> origList, EntryResolverRegistry resolvers, Account account)
   {
      if (origList == null)
         return null;

      return origList.stream()
            .map(proxy -> toDTO(proxy, resolvers, account))
            .collect(Collectors.toList());
   }

   public static RestApiV1.RelationshipSearchResult toDTO(RelnSearchProxy orig, EntryResolverRegistry resolvers, Account account)
   {
      if (orig == null)
      {
         return null;
      }

      RestApiV1.RelationshipSearchResult dto = new RestApiV1.RelationshipSearchResult();
      dto.id = orig.id;
      dto.ref = orig.token == null ? null : EntryIdDto.adapt(resolvers.getReference(orig.token));
      dto.description = orig.description;
      dto.typeId = orig.typeId;

      dto.related.clear();
      orig.related.stream()
            .map(token -> createAnchor(token, resolvers, account))
            .forEach(dto.related::add);

      dto.targets.clear();
      orig.targets.stream()
            .map(token -> createAnchor(token, resolvers, account))
            .forEach(dto.targets::add);

      return dto;
   }

   private static RestApiV1.Anchor createAnchor(RelnSearchProxy.Anchor anchor, EntryResolverRegistry resolvers, Account account)
   {
      EntryId entryId = new EntryId(anchor.ref.id, anchor.ref.type);

      RestApiV1.Anchor dto = new RestApiV1.Anchor();

      dto.ref = new EntryIdDto();
      dto.ref.id = anchor.ref.id;
      dto.ref.type = anchor.ref.type;
      dto.ref.token = anchor.ref.token;

      EntryResolver<Object> resolver = resolvers.getResolver(entryId);
      Optional<Object> instance = resolver.resolve(account, entryId);

      dto.label = instance.map(entry -> resolver.getLabel(entry))
                          .orElse("Invalid Anchor Reference");

      return dto;
   }
}
