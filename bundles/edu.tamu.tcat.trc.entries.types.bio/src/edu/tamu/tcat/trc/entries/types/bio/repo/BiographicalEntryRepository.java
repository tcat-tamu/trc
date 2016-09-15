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
package edu.tamu.tcat.trc.entries.types.bio.repo;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.NoSuchEntryException;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;

/**
 *  Primary point of access for obtaining and creating biographical entries.
 */
public interface BiographicalEntryRepository extends EntryRepository<BiographicalEntry>
{

   /** The type id used to identify biographical entries within the EntryResolver framework. */
   public final static String ENTRY_TYPE_ID = "trc.entries.biographical";

   /** The initial path (relative to some API endpoint) for building URIs that reference
    *  a biographical entry and its sub-elements. */
   public final static String ENTRY_URI_BASE = "entries/biographical";

   @Override
   BiographicalEntry get(String id) throws NoSuchEntryException;

   @Override
   Iterator<BiographicalEntry> listAll();

   @Override
   EditBiographicalEntryCommand create();

   @Override
   EditBiographicalEntryCommand create(String id);


   @Override
   EditBiographicalEntryCommand edit(String id) throws NoSuchEntryException;

   @Override
   CompletableFuture<Boolean> remove(String id) throws NoSuchEntryException;

   @Override
   EntryRepository.ObserverRegistration onUpdate(EntryRepository.UpdateObserver<BiographicalEntry> observer);

}
