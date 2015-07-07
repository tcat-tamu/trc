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
package edu.tamu.tcat.trc.entries.core;

/**
 * A service for creating identifiers for catalog entries. Identifiers are guaranteed to be
 * unique within the scope of a named context, but may conflict across scopes. Contexts are
 * intended to be used to generate unique id sequences for diffrent types of catalog entries
 * (e.g., bibliographic entries and biographical entries have their own sequence of
 * identifiers).
 *
 * <p>
 * Implementations are free to provide there own identification schemes. Notably, there is
 * no guarantee that the values returned will be numeric.
 */
public interface IdFactory
{
   
   // Twitter has encountered a similar need for generating coherent IDs and has developed Snowflake:
   // https://blog.twitter.com/2010/announcing-snowflake
   
   /**
    * Generate a new id for the named context.
    *
    * @param context The context for the id to be generated.
    * @return A new unique identifier for the supplied context.
    */
   String getNextId(String context);
}
