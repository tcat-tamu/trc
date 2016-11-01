package edu.tamu.tcat.trc.entries.types.biblio.impl.search;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.impl.repo.ModelAdapter;
import edu.tamu.tcat.trc.entries.types.biblio.search.BiblioSearchProxy;
import edu.tamu.tcat.trc.search.solr.SearchException;

public abstract class IndexAdapter
{
   public static SolrInputDocument createWork(BibliographicEntry entry)
   {
      return createWork(WorkDTO.create(entry));
   }

   public static SolrInputDocument createWork(WorkDTO workDTO)
   {
      try
      {
         BiblioDocument doc = new BiblioDocument();

         doc.indexDocument.set(BiblioSolrConfig.ID, workDTO.id);
         doc.addAuthors(workDTO.authors);
         doc.addTitles(workDTO.titles);
         doc.indexDocument.set(BiblioSolrConfig.SUMMARY, workDTO.summary);
//         doc.indexDocument.set(BiblioSolrConfig.ENTRY_REFERENCE, entryRef);

         try
         {
            doc.indexDocument.set(BiblioSolrConfig.SEARCH_PROXY, BiblioSearchProxy.create(ModelAdapter.adapt(workDTO)));
         }
         catch (Exception e)
         {
            throw new IllegalStateException("Failed to serialize BiblioSearchProxy data", e);
         }

         return doc.indexDocument.build();
      }
      catch (SearchException se)
      {
         throw new IllegalStateException("Failed to create indexable document.", se);
      }
   }

   public static SolrInputDocument updateWork(BibliographicEntry work) throws SearchException
   {
      BiblioDocument doc = new BiblioDocument();
      WorkDTO workDTO = WorkDTO.create(work);

      doc.indexDocument.update(BiblioSolrConfig.ID, workDTO.id);
      doc.updateAuthors(workDTO.authors);
      doc.updateTitles(workDTO.titles);
      doc.indexDocument.update(BiblioSolrConfig.SUMMARY, workDTO.summary);
//         doc.indexDocument.set(BiblioSolrConfig.ENTRY_REFERENCE, entryRef);

      try
      {
         doc.indexDocument.update(BiblioSolrConfig.SEARCH_PROXY, BiblioSearchProxy.create(work));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize BiblioSearchProxy data", e);
      }
      return doc.indexDocument.build();
   }
}