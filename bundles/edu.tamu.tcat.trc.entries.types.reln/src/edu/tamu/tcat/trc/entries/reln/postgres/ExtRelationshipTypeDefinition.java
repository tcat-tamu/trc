package edu.tamu.tcat.trc.entries.reln.postgres;

import org.eclipse.core.runtime.IConfigurationElement;

import edu.tamu.tcat.trc.entries.reln.RelationshipType;

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
