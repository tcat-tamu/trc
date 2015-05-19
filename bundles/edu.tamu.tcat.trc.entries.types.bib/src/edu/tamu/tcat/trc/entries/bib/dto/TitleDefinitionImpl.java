package edu.tamu.tcat.trc.entries.bib.dto;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.bib.Title;
import edu.tamu.tcat.trc.entries.bib.TitleDefinition;

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
                   .filter(t -> t.getType().equalsIgnoreCase("canonical"))
                   .findAny()
                   .orElse(titles.stream().findAny().orElse(null));
   }

   @Override
   public Title getShortTitle()
   {
      return titles.stream()
            .filter(t -> t.getType().equalsIgnoreCase("short"))
            .findAny()
            .orElse(null);
   }

   @Override
   public Set<Title> getAlternateTitles()
   {
      return titles.stream()
            .filter(t -> !t.getType().equalsIgnoreCase("canonical"))
            .collect(Collectors.toSet());
   }

   @Override
   public Title getTitle(Locale language)
   {
      return titles.stream()
            .filter(t -> t.getLanguage().equalsIgnoreCase(language.getLanguage()))
            .findAny()
            .orElse(null);
   }
}
