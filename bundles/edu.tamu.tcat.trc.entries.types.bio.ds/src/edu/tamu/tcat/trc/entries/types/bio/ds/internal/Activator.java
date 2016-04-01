package edu.tamu.tcat.trc.entries.types.bio.ds.internal;
/*
 * Copyright 2014 Texas A&M Engineering Experiment Station
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

import edu.tamu.tcat.osgi.services.util.ActivatorBase;

public class Activator extends ActivatorBase
{
   private static Activator instance;

   public Activator()
   {
      instance = this;
   }

   public static Activator getDefault()
   {
      return instance;
   }
}
