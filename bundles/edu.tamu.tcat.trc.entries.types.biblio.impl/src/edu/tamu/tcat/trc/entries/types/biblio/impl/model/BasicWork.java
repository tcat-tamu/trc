package edu.tamu.tcat.trc.entries.types.biblio.impl.model;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.TitleDefinition;
import edu.tamu.tcat.trc.entries.types.biblio.impl.repo.DataModelV1;

public class BasicWork implements BibliographicEntry
{
   private final String id;
   private final String type;
   private final List<AuthorReference> authors;
   private final TitleDefinition title;
   private final List<AuthorReference> otherAuthors;

//   private final AuthorList authors;
//   private final AuthorList otherAuthors;
   private final List<Edition> editions;
   private final String series;
   private final String summary;
   private final CopyReference defaultCopyReference;
   private final Set<CopyReference> copyReferences;

   public BasicWork(DataModelV1.WorkDTO dto)
   {
      this.id = dto.id;
      this.type = dto.type;

      this.series = dto.series;
      this.summary = dto.summary;

      this.authors = dto.authors != null
            ? dto.authors.stream().map(BasicAuthorReference::new).collect(toList())
            : Collections.emptyList();

      Collection<Title> titles = dto.titles != null
            ? dto.titles.stream().map(BasicTitle::new).collect(toList())
            : Collections.emptyList();
      this.title = new BasicTitleDefinition(titles);

      this.editions = dto.editions != null
            ? dto.editions.stream().map(BasicEdition::new).collect(toList())
            : Collections.emptyList();

      this.otherAuthors = dto.otherAuthors != null
            ? dto.otherAuthors.stream().map(BasicAuthorReference::new).collect(toList())
            : Collections.emptyList();

      this.copyReferences = dto.copyReferences != null
            ? dto.copyReferences.stream().map(BasicCopyReference::new).collect(toSet())
            : Collections.emptySet();

      this.defaultCopyReference = copyReferences.stream()
            .filter(copyReference -> Objects.equals(copyReference.getId(), dto.defaultCopyReferenceId))
            .findFirst()
            .orElse(!copyReferences.isEmpty() ? copyReferences.iterator().next() : null);
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   @Deprecated
   public String getType()
   {
      return type;
   }

   @Override
   public List<AuthorReference> getAuthors()
   {
      return Collections.unmodifiableList(authors);
   }

   @Override
   public TitleDefinition getTitle()
   {
      return title;
   }

   @Override
   public Optional<Title> getTitle(String... types)
   {
      TreeSet<Title> titles = new TreeSet<>(BasicWork::compare);
      titles.addAll(getTitle().get());

      if (titles.isEmpty())
         return Optional.empty();

      Optional<Title> title = Optional.empty();
      for (String type : types)
      {
         title = titles.stream()
            .filter(t -> type.equalsIgnoreCase(t.getType()))
            .findAny();

         if (title.isPresent())
            break;
      }

      return title.isPresent() ? title : Optional.of(titles.iterator().next());
   }

   private static int compare(Title a, Title b)
   {
      int score = a.getFullTitle().compareTo(b.getFullTitle());
      return (score != 0)
            ? score : Integer.compare(a.hashCode(), b.hashCode());
   }

   @Override
   public List<AuthorReference> getOtherAuthors()
   {
      return Collections.unmodifiableList(otherAuthors);
   }

   @Override
   public List<Edition> getEditions()
   {
      return editions;
   }

   @Override
   public Edition getEdition(String editionId)
   {
      for (Edition edition : editions) {
         if (edition.getId().equals(editionId)) {
            return edition;
         }
      }

      return null;
   }

   @Override
   public String getSeries()
   {
      return series;
   }

   @Override
   public String getSummary()
   {
      return summary;
   }

   @Override
   public CopyReference getDefaultCopyReference()
   {
      return defaultCopyReference;
   }

   @Override
   public Set<CopyReference> getCopyReferences()
   {
      return copyReferences;
   }
}