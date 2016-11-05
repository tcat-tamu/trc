package edu.tamu.tcat.trc.entries.types.biblio.impl.search;

import static java.util.stream.Collectors.toSet;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.EntryFacade;
import edu.tamu.tcat.trc.ResourceNotFoundException;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.BiblioEntryUtils;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.TitleDefinition;
import edu.tamu.tcat.trc.entries.types.biblio.search.BiblioSearchProxy;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolver;
import edu.tamu.tcat.trc.search.solr.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;

public abstract class IndexAdapter
{
   // TODO see SolrDocAdapter in bio for an alternative impl strategy

   public static SolrInputDocument createWork(EntryFacade<BibliographicEntry> entry)
   {
      try
      {
         BibliographicEntry work = entry.getEntry()
               .orElseThrow(() -> new ResourceNotFoundException());

         TrcDocument doc = new TrcDocument(new BiblioSolrConfig());

         doc.set(BiblioSolrConfig.ID, work.getId());
         doc.set(BiblioSolrConfig.SUMMARY, work.getSummary());
         doc.set(BiblioSolrConfig.ENTRY_REFERENCE, entry.getToken());

         work.getAuthors().stream().forEach(author -> {
            doc.set(BiblioSolrConfig.AUTHOR_IDS, author.getId() != null ? author.getId() : "");
            doc.set(BiblioSolrConfig.AUTHOR_NAMES, author.getFirstName() + " " + author.getLastName());
         });

         Set<String> titles = getTitles(work);
         titles.stream().forEach(title -> doc.set(BiblioSolrConfig.TITLES, title));

         doc.set(BiblioSolrConfig.SEARCH_PROXY, toProxy(entry));

         return doc.build();
      }
      catch (SearchException se)
      {
         throw new IllegalStateException("Failed to create indexable document.", se);
      }
   }

   public static SolrInputDocument updateWork(EntryFacade<BibliographicEntry> entry)
   {
      try
      {
         BibliographicEntry work = entry.getEntry()
               .orElseThrow(() -> new ResourceNotFoundException());

         TrcDocument doc = new TrcDocument(new BiblioSolrConfig());

         doc.update(BiblioSolrConfig.ID, work.getId());
         doc.update(BiblioSolrConfig.SUMMARY, work.getSummary());
         doc.update(BiblioSolrConfig.ENTRY_REFERENCE, entry.getToken());

         doc.update(BiblioSolrConfig.AUTHOR_IDS,
               work.getAuthors().stream()
                     .map(author -> author.getId() != null ? author.getId() : "")
                     .collect(toSet())
               );
         doc.update(BiblioSolrConfig.AUTHOR_NAMES,
               work.getAuthors().stream()
               .map(author -> author.getFirstName() + " " + author.getLastName())
               .collect(toSet())
               );
         doc.update(BiblioSolrConfig.TITLES, getTitles(work));
         doc.set(BiblioSolrConfig.SEARCH_PROXY, toProxy(entry));

         return doc.build();
      }
      catch (SearchException se)
      {
         throw new IllegalStateException("Failed to create indexable document.", se);
      }
   }

   private static Set<String> getTitles(BibliographicEntry work)
   {
      TitleDefinition titles = work.getTitle();
      Set<String> strTitles = titles.getTypes().stream()
            .map(titles::get)
            .map(IndexAdapter::getTitle)
            .filter(t -> !Objects.isNull(t))
            .collect(toSet());

      return strTitles;
   }

   private static String getTitle(Optional<Title> title)
   {
      // HACK this replaces a block of stream oriented logic that compiled on
      //      dev machines but not on the build server.

      return title.map(Title::getFullTitle).orElse(null);
   }

   private static BiblioSearchProxy toProxy(EntryFacade<BibliographicEntry> entry)
   {
      EntryId entryId = entry.getEntryId();
      EntryResolver<BibliographicEntry> resolver = entry.getResolver();
      BibliographicEntry work = entry.getEntry().orElseThrow(() -> new ResourceNotFoundException());

      BiblioSearchProxy result = new BiblioSearchProxy();

      result.id = entryId.getId();
      result.uri = resolver.toUri(entryId).toString();
      result.token = entry.getToken();
      result.type = work.getType();

      result.authors = work.getAuthors().stream()
            .map(IndexAdapter::adapt)
            .collect(Collectors.toList());
      result.title = BiblioEntryUtils.parseTitle(work, Title.SHORT, Title.CANONICAL)
            .orElse("No Title Available");
      result.label = resolver.getHtmlLabel(work);
      result.pubYear = BiblioEntryUtils.parsePublicationDate(work);

      result.summary = work.getSummary();

      return result;
   }

   private static BiblioSearchProxy.AuthorProxy adapt(AuthorReference author)
   {
      BiblioSearchProxy.AuthorProxy dto = new BiblioSearchProxy.AuthorProxy();
      if (author == null)
         return dto;

      dto.authorId = author.getId();
      dto.firstName = guardNull(author.getFirstName());
      dto.lastName = guardNull(author.getLastName());
      dto.role = guardNull(author.getRole());

      return dto;
   }

   private static String guardNull(String str)
   {
      return (str == null || str.trim().isEmpty()) ? "" : str.trim();
   }
}