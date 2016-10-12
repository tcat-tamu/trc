package edu.tamu.tcat.trc.entries.types.biblio.impl.search;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.VolumeDTO;
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
         doc.indexDocument.set(BiblioSolrConfig.TYPE, workDTO.type);
         doc.addAuthors(workDTO.authors);
         doc.addTitles(workDTO.titles);
         doc.indexDocument.set(BiblioSolrConfig.SERIES, workDTO.series);
         doc.indexDocument.set(BiblioSolrConfig.SUMMARY, workDTO.summary);

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

   public static SolrInputDocument createEdition(String workId, Edition edition) throws SearchException
   {
      EditionDTO editionDTO = EditionDTO.create(edition);
      StringBuilder editionId = new StringBuilder(workId)
            .append(":")
            .append(editionDTO.id);

      BiblioDocument doc = new BiblioDocument();
      doc.indexDocument.set(BiblioSolrConfig.ID, editionId.toString());
      doc.indexDocument.set(BiblioSolrConfig.EDITION_NAME, editionDTO.editionName);
      doc.addAuthors(editionDTO.authors);
      doc.addTitles(editionDTO.titles);
      doc.addPublication(editionDTO.publicationInfo);
      doc.indexDocument.set(BiblioSolrConfig.SERIES, editionDTO.series);
      doc.indexDocument.set(BiblioSolrConfig.SUMMARY, editionDTO.summary);

      try
      {
         doc.indexDocument.set(BiblioSolrConfig.SEARCH_PROXY, BiblioSearchProxy.create(workId, edition));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize BiblioSearchProxy data", e);
      }
      return doc.indexDocument.build();
   }

   public static SolrInputDocument createVolume(String workId, Edition edition, Volume volume) throws SearchException
   {
      VolumeDTO volumeDTO = VolumeDTO.create(volume);
      StringBuilder volumeId = new StringBuilder(workId)
            .append(":")
            .append(edition.getId())
            .append(":")
            .append(volumeDTO.id);

      BiblioDocument doc = new BiblioDocument();
      doc.indexDocument.set(BiblioSolrConfig.ID, volumeId.toString());
      doc.indexDocument.set(BiblioSolrConfig.EDITION_NAME, edition.getEditionName());
      doc.indexDocument.set(BiblioSolrConfig.VOLUME_NUMBER, volumeDTO.volumeNumber);
      doc.addAuthors(volumeDTO.authors);
      doc.addTitles(volumeDTO.titles);
      doc.addPublication(volumeDTO.publicationInfo);
      doc.indexDocument.set(BiblioSolrConfig.SERIES, volumeDTO.series);
      doc.indexDocument.set(BiblioSolrConfig.SUMMARY, volumeDTO.summary);

      try
      {
         doc.indexDocument.set(BiblioSolrConfig.SEARCH_PROXY, BiblioSearchProxy.create(workId, edition.getId(), volume));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize BiblioSearchProxy data", e);
      }
      return doc.indexDocument.build();
   }

   public static SolrInputDocument updateWork(BibliographicEntry work) throws SearchException
   {
      BiblioDocument doc = new BiblioDocument();
      WorkDTO workDTO = WorkDTO.create(work);

      doc.indexDocument.update(BiblioSolrConfig.ID, workDTO.id);
      doc.indexDocument.update(BiblioSolrConfig.TYPE, workDTO.type);
      doc.updateAuthors(workDTO.authors);
      doc.updateTitles(workDTO.titles);
      doc.indexDocument.update(BiblioSolrConfig.SERIES, workDTO.series);
      doc.indexDocument.update(BiblioSolrConfig.SUMMARY, workDTO.summary);

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

   public static SolrInputDocument updateEdition(String workId, Edition edition) throws SearchException
   {
      EditionDTO editionDTO = EditionDTO.create(edition);
      StringBuilder editionId = new StringBuilder(workId)
            .append(":")
            .append(editionDTO.id);

      BiblioDocument doc = new BiblioDocument();
      doc.indexDocument.update(BiblioSolrConfig.ID, editionId.toString());
      doc.indexDocument.update(BiblioSolrConfig.EDITION_NAME, editionDTO.editionName);
      doc.updateAuthors(editionDTO.authors);
      doc.updateTitles(editionDTO.titles);
      doc.updatePublication(editionDTO.publicationInfo);
      doc.indexDocument.update(BiblioSolrConfig.SERIES, editionDTO.series);
      doc.indexDocument.update(BiblioSolrConfig.SUMMARY, editionDTO.summary);

      try
      {
         doc.indexDocument.update(BiblioSolrConfig.SEARCH_PROXY, BiblioSearchProxy.create(workId, edition));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize BiblioSearchProxy data", e);
      }
      return doc.indexDocument.build();
   }

   public static SolrInputDocument updateVolume(String workId, Edition edition, Volume volume) throws SearchException
   {
      VolumeDTO volumeDTO = VolumeDTO.create(volume);
      StringBuilder volumeId = new StringBuilder(workId)
            .append(":")
            .append(edition.getId())
            .append(":")
            .append(volumeDTO.id);

      BiblioDocument doc = new BiblioDocument();
      doc.indexDocument.update(BiblioSolrConfig.ID, volumeId.toString());
      doc.indexDocument.update(BiblioSolrConfig.EDITION_NAME, edition.getEditionName());
      doc.indexDocument.update(BiblioSolrConfig.VOLUME_NUMBER, volumeDTO.volumeNumber);
      doc.updateAuthors(volumeDTO.authors);
      doc.updateTitles(volumeDTO.titles);
      doc.updatePublication(volumeDTO.publicationInfo);
      doc.indexDocument.update(BiblioSolrConfig.SERIES, volumeDTO.series);
      doc.indexDocument.update(BiblioSolrConfig.SUMMARY, volumeDTO.summary);

      try
      {
         doc.indexDocument.update(BiblioSolrConfig.SEARCH_PROXY, BiblioSearchProxy.create(workId, edition.getId(), volume));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize BiblioSearchProxy data", e);
      }
      return doc.indexDocument.build();
   }
}