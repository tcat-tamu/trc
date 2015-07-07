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
package edu.tamu.tcat.trc.entries.types.reln.postgres;

import org.eclipse.core.runtime.IConfigurationElement;

import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;

public class ExtRelationshipTypeDefinition implements RelationshipType
{
   private final String id;
   private final String title;
   private final String reverse;
   private final String description;
   private final boolean isDirected;

   private final IConfigurationElement config;

   public ExtRelationshipTypeDefinition(IConfigurationElement e)
   {
      config = e;
      id = config.getAttribute("identifier");
      title = config.getAttribute("title");
      reverse = config.getAttribute("reverse_title");
      description = config.getAttribute("description");
      String str = config.getAttribute("is_directed");
      isDirected = Boolean.parseBoolean(str);
   }

   @Override
   public String getIdentifier()
   {
      return id;
   }

   @Override
   public String getTitle()
   {
      return title;
   }

   @Override
   public String getReverseTitle()
   {
      return reverse;
   }

   @Override
   public String getDescription()
   {
      return description;
   }

   @Override
   public boolean isDirected()
   {
      return isDirected;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof RelationshipType))
      {
         // NOTE: not strictly compliant with #equals contract since this may not be reversible with other impls.
         return false;
      }

      return this.id.equals(((RelationshipType)obj).getIdentifier());
   }

   @Override
   public int hashCode()
   {
      return id.hashCode();
   }

   @Override
   public String toString()
   {
      return "Relationship Type: " + title + " [" + id + "]";
   }
}
