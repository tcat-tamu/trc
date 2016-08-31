package edu.tamu.tcat.trc.entries.types.bio.postgres.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.common.HistoricalEvent;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.entries.types.bio.postgres.DataModelV1;

public class PersonImpl implements Person
{
   private final String id;
   private final PersonName canonicalName;
   private final Set<PersonName> names;
   private final HistoricalEventImpl birth;
   private final HistoricalEventImpl death;
   private final String summary;

   public PersonImpl(DataModelV1.Person figure)
   {
      this.id = figure.id;
      this.canonicalName = new PersonNameImpl(getCanonicalName(figure));
      
      if (figure.names != null)
         this.names = figure.names.stream()
                     .map(PersonNameImpl::new)
                     .collect(Collectors.toSet());
      else
         this.names = new HashSet<>();
      
      this.birth = (figure.birth != null) ? new HistoricalEventImpl(figure.birth) : null;
      this.death = (figure.death != null) ? new HistoricalEventImpl(figure.death) : null;

      this.summary = figure.summary;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public PersonName getCanonicalName()
   {
      return canonicalName;
   }

   @Override
   public Set<PersonName> getAlternativeNames()
   {
      return Collections.unmodifiableSet(names);
   }

   @Override
   public HistoricalEvent getBirth()
   {
      return birth;
   }

   @Override
   public HistoricalEvent getDeath()
   {
      return death;
   }

   @Override
   public String getSummary()
   {
      return summary;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      // use canonical name for display purposes
      PersonName name = canonicalName;

      // fall back to first element of names
      if (name == null && !names.isEmpty()) {
         name = names.iterator().next();
      }

      if (name != null) {
         if (name.getDisplayName() != null)
         {
            sb.append(name.getDisplayName());
         }
         else
         {
            String fn = name.getFamilyName();
            String gn = name.getGivenName();
            if (fn != null && !fn.trim().isEmpty()) {
               sb.append(fn.trim());
            }

            if (gn != null && !gn.trim().isEmpty())
            {
               if (sb.length() > 0) {
                  sb.append(", ");
               }

               sb.append(gn.trim());
            }
         }

         // TODO append dates
      }

      return sb.toString();
   }

   /**
    * Get a canonical name from a person DV. Prefer to use the displayName field, but fall back to
    * the first element in the 'names' set if a displayName is not available.
    *
    * @param figure
    * @return canonical name for this person
    */
   private static DataModelV1.PersonName getCanonicalName(DataModelV1.Person figure)
   {
      // try the 'displayName' first
      if (figure.displayName != null) {
         return figure.displayName;
      }

      // fall back to using the first element of the 'names' set
      if (!figure.names.isEmpty()) {
         return figure.names.iterator().next();
      }

      // fall back to "Name Unknown" if this person does not have any names
      DataModelV1.PersonName fallbackName = new DataModelV1.PersonName();
      fallbackName.displayName = "Name Unknown";
      return fallbackName;
   }

   // equals and hash code?
}