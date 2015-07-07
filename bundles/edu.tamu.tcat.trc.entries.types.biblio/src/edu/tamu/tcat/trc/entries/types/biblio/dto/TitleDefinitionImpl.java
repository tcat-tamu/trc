package edu.tamu.tcat.trc.entries.types.biblio.dto;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.TitleDefinition;

public class TitleDefinitionImpl implements TitleDefinition
{
   private Set<Title> titles = new HashSet<>();

   public TitleDefinitionImpl(Collection<TitleDV> titles)
   {
      this.titles = titles.stream().map(TitleDV::instantiate).collect(Collectors.toSet());
   }

   @Override
   public Title getCanonicalTitle()
   {
      return titles.stream()
                   .filter(t -> "canonical".equalsIgnoreCase(t.getType()))
                   .findAny()
                   .orElse(titles.stream().findAny().orElse(null));
   }

   @Override
   public Title getShortTitle()
   {
      return titles.stream()
            .filter(t -> "short".equalsIgnoreCase(t.getType()))
            .findAny()
            .orElse(null);
   }

   @Override
   public Set<Title> getAlternateTitles()
   {
      return titles.stream()
            .filter(t -> !"canonical".equalsIgnoreCase(t.getType()))
            .collect(Collectors.toSet());
   }

   @Override
   public Title getTitle(Locale language)
   {
      return titles.stream()
            .filter(t -> language.getLanguage().equalsIgnoreCase(t.getLanguage()))
            .findAny()
            .orElse(null);
   }
}
