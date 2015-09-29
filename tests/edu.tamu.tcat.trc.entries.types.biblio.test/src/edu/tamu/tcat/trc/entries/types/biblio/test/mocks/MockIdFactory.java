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
package edu.tamu.tcat.trc.entries.types.biblio.test.mocks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import edu.tamu.tcat.trc.persist.IdFactory;

public class MockIdFactory implements IdFactory
{
   private ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<String, AtomicInteger>();

   @Override
   public String getNextId(String context) {
      if (!counters.contains(context))
      {
         counters.putIfAbsent(context, new AtomicInteger(0));
      }

      int id = counters.get(context).incrementAndGet();
      return String.valueOf(id);
   };
}
