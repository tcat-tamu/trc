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
package edu.tamu.tcat.trc.entries.types.biblio.search.solr.copies;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import edu.tamu.tcat.hathitrust.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.htrc.features.simple.ExtractedFeatures;
import edu.tamu.tcat.hathitrust.htrc.features.simple.impl.DefaultExtractedFeaturesProvider;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.notification.UpdateEvent.UpdateAction;
import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyChangeEvent;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceRepository;
import edu.tamu.tcat.trc.entries.types.biblio.search.copies.FullTextSearchService;
import edu.tamu.tcat.trc.entries.types.biblio.search.copies.PageSearchCommand;
import edu.tamu.tcat.trc.entries.types.biblio.search.copies.VolumeSearchCommand;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;

public class SolrCopyReferenceIndexManager implements FullTextSearchService
{
   // TODO remove dependency on HTRC to copy implementation.

   private static Logger logger = Logger.getLogger(SolrCopyReferenceIndexManager.class.getName());
   private static final Pattern copyIdPattern = Pattern.compile("^htid:(\\d{9})#(.*)$");

   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";

   /** Configuration property key that defines the path to the HathiTrust extracted
    *  features data set to be used to pull full text information about a copy. */
   // HACK need a more flexible mechanism for defining how to retrieve the full text
   //      of a digital copy.
   public static final String HTRC_EX_FEATURES_PATH = "htrc.extracted_features.path";

   /** Configuration property key that defines Solr core to be used volume-level full text. */
   public static final String SOLR_CORE_VOLS = "trc.entries.bib.copies.volumes.core";

   /** Configuration property key that defines Solr core to be used page-level full text. */
   public static final String SOLR_CORE_PAGES = "trc.entries.bib.copies.pages.core";

   private SolrServer solrVols;
   private SolrServer solrPages;

   private ConfigurationProperties config;
   private CopyReferenceRepository repo;

   private DefaultExtractedFeaturesProvider provider;
   private AutoCloseable registration;

   /**
    * Supply the configure properties to be used
    * <p>Intended to be called by the OSGi DS framework.
    * @param config
    */
   public void setConfig(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void setCopyRefRepo(CopyReferenceRepository repo)
   {
      this.repo = repo;
   }

   public void activate()
   {
      registration = repo.register(this::onUpdate);

      Path exFeatPath = config.getPropertyValue(SOLR_API_ENDPOINT, Path.class);
      provider = new DefaultExtractedFeaturesProvider(exFeatPath);

      URI solrBaseURI = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);
      String volCore = config.getPropertyValue(SOLR_CORE_VOLS, String.class, "volumes");
      URI volURI = solrBaseURI.resolve(volCore);
      solrVols = new HttpSolrServer(volURI.toString());

      String pageCore = config.getPropertyValue(SOLR_CORE_PAGES, String.class, "pages");
      URI pageURI = solrBaseURI.resolve(pageCore);
      solrPages = new HttpSolrServer(pageURI.toString());
   }

   public void deactivate()
   {
      unregisterRepoListener();

      releaseSolrConnection(solrVols);
      releaseSolrConnection(solrPages);

      releaseFeaturesProvider();
   }

   @Override
   public VolumeSearchCommand getVolumeSearchCommand() throws SearchException
   {
      TrcQueryBuilder builder = new TrcQueryBuilder(solrVols, new FullTextVolumeConfig());
      return new VolumeSolrSearchCommand(solrVols, builder);
   }

   @Override
   public PageSearchCommand getPageSearchCommand() throws SearchException
   {
      return new PageSolrSearchCommand(solrPages, new TrcQueryBuilder(solrPages, new FullTextPageConfig()));
   }

   private void releaseFeaturesProvider()
   {
      if (provider == null)
         return;

      try
      {
         provider.close();
      }
      catch (Exception e)
      {
         logger.log(Level.WARNING, "Failed to cleanly shut down FeatureProvider.", e);
      }
      finally
      {
         provider = null;
      }
   }

   private void unregisterRepoListener()
   {
      if (registration != null)
      {
         try
         {
            registration.close();
         }
         catch (Exception e)
         {
            logger.log(Level.WARNING, "Failed to unregister update listener on people repository.", e);
         }
         finally {
            registration = null;
         }
      }
   }

   private void releaseSolrConnection(SolrServer solr)
   {
      logger.fine("Releasing connection to Solr server");
      if (solr == null)
         return;

      try
      {
         solr.shutdown();
      }
      catch (Exception e)
      {
         logger.log(Level.WARNING, "Failed to cleanly shut down connection to Solr server.", e);
      }
   }

   private void onUpdate(CopyChangeEvent evt)
   {
      // TODO Auto-generated method stub
      UpdateAction action = evt.getUpdateAction();
      try
      {
         switch (action)
         {
            case CREATE: onCreate(evt.getOriginal());
               break;
            case UPDATE: onUpdate(evt.get());
               break;
            case DELETE: onDelete(evt.get());
               break;
         }
      }
      catch (CatalogRepoException e)
      {
         logger.log(Level.WARNING, "Failed to update full text index for digital copy '" + evt.getEntityId() + "' on update action " + action, e);
      }
   }

   private void onCreate(CopyReference copyRef)
   {
      String copyId = copyRef.getCopyId();

      try
      {
         Collection<PageTextDocument> pages = getPageText(copyRef);

         String associatedEntry = copyRef.getAssociatedEntry().toString();
         String volumeText = pages.parallelStream()
               .map(PageTextDocument::getText)
               .collect(Collectors.joining(" "));
         VolumeTextDocument volume = VolumeTextDocument.create(copyId, associatedEntry, volumeText);

         solrPages.add(pages.parallelStream()
               .map(PageTextDocument::getDocument)
               .collect(Collectors.toList()));
         solrVols.add(volume.getDocument());
         solrPages.commit();
         solrVols.commit();
      }
      catch(Exception e)
      {
         logger.log(Level.SEVERE, "Failed processing page to solr [" + copyId + "]", e);
      }
   }

   private String getHtid(CopyReference copyRef)
   {
      String copyId = copyRef.getCopyId();
      Matcher matcher = copyIdPattern.matcher(copyId);
      if (!matcher.find())
         throw new IllegalArgumentException("Id does not match current patterns: " + copyId);

      String htid = matcher.group(2);
      return htid;
   }

   private void onUpdate(CopyReference copyRef)
   {

   }

   private void onDelete(CopyReference copyRef)
   {

   }

   private Collection<PageTextDocument> getPageText(CopyReference copyRef)
         throws HathiTrustClientException, Exception
   {
      // TODO need a general puprose mechanism for extracting this information that is
      //      not tied to SOLR. This seems like a common requirement.
      String htid = getHtid(copyRef);
      ExtractedFeatures feature = provider.getExtractedFeatures(htid);

      List<PageTextDocument> pages = new ArrayList<>();
      int pageCount = feature.pageCount();
      for (int pg = 0; pg < pageCount; ++pg)
      {
         String pgId = htid + ":" + pg;
         String pgText = extractPageTokens(feature, pg);
         pages.add(PageTextDocument.create(pgId, pg, pgText));
      }

      return pages;
   }

   /**
    * Creates a synthetic representation of the content of a page that contains the words of
    * the page as represented within the HTRC extracted feature set are present in the same
    * number as on the original page, but arbitrarily ordered (alphabetically with each word
    * repeated the same number of time it occurs in the original page).
    *
    * @param feat
    * @param pg
    * @return
    * @throws HathiTrustClientException
    */
   private String extractPageTokens(ExtractedFeatures feat, int pg) throws HathiTrustClientException
   {
      // HACK: this needs to be generalized so that we can get text from a wide range of source copies
      ExtractedFeatures.ExtractedPageFeatures page = feat.getPage(pg);
      ExtractedFeatures.ExtractedPagePartOfSpeechData bodyData = page.getBodyData();
      Set<String> toks = bodyData.tokens();

      StringBuilder pageContents = new StringBuilder();
      toks.stream().sorted()
          .forEach(token -> {
             try
             {
                int count = bodyData.getCount(token);
                Collections.nCopies(count, token).stream().forEach(s -> pageContents.append(s).append(" "));
             }
             catch (Exception ex)
             {
                logger.log(Level.SEVERE, "Failed to represent token: " + token, ex);
             }
          });

      return pageContents.toString();
   }
}
