package edu.tamu.tcat.trc.entries.types.bib.copies.search.solr;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.hathitrust.htrc.features.simple.ExtractedFeatures;
import edu.tamu.tcat.hathitrust.htrc.features.simple.impl.DefaultExtractedFeaturesProvider;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.notification.UpdateEvent.UpdateAction;
import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.entries.types.bib.copies.CopyReference;
import edu.tamu.tcat.trc.entries.types.bib.copies.repo.CopyChangeEvent;
import edu.tamu.tcat.trc.entries.types.bib.copies.repo.CopyReferenceRepository;

public class SolrCopyReferenceIndex implements CopyReferenceIndex
{
   private static Logger logger = Logger.getLogger(SolrCopyReferenceIndex.class.getName());
   private static final Pattern copyIdPattern = Pattern.compile("^htid:(\\d{9})#(.*)$");

   private static final String FILES_PATH_ROOT = "\\\\citd.tamu.edu\\citdfs\\archive\\HTRC_Dataset\\";

   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";

   /** Configuration property key that defines Solr core to be used for relationships. */
   public static final String SOLR_CORE_VOLS = "trc.entries.bib.copies.volumes.core";
   public static final String SOLR_CORE_PAGES = "trc.entries.bib.copies.pages.core";

   private SolrServer solrVols;
   private SolrServer solrPages;

   private ConfigurationProperties config;

   private CopyReferenceRepository repo;

   private DefaultExtractedFeaturesProvider provider;

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

      provider = new DefaultExtractedFeaturesProvider(Paths.get(FILES_PATH_ROOT));
      repo.register(new UpdateListener<CopyChangeEvent>()
      {
         @Override
         public void handle(CopyChangeEvent evt)
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
      });

      URI solrBaseURI = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);
      String volCore = config.getPropertyValue(SOLR_CORE_VOLS, String.class, "volumes");
      String pageCore = config.getPropertyValue(SOLR_CORE_PAGES, String.class, "pages");

      URI volURI = solrBaseURI.resolve(volCore);
      URI pageURI = solrBaseURI.resolve(pageCore);

      solrVols = new HttpSolrServer(volURI.toString());
      solrPages = new HttpSolrServer(pageURI.toString());
   }

   public void deactivate()
   {
      releaseSolrConnection(solrVols);
      releaseSolrConnection(solrPages);
      try
      {
         provider.close();
      }
      catch (Exception e)
      {
         logger.log(Level.WARNING, "Failed to cleanly shut down FeatureProvider.", e);
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

   private void onCreate(CopyReference copyRef)
   {
      PageSolrProxy pageDoc = new PageSolrProxy();
      Collection<SolrInputDocument> pageDocs = new HashSet<>();
      try
      {
         String copyId = copyRef.getCopyId();
         Matcher matcher = copyIdPattern.matcher(copyId);
         if (!matcher.find())
            throw new IllegalArgumentException("Id does not match current patterns: " + copyId);

         ExtractedFeatures feature = provider.getExtractedFeatures(matcher.group(2));

         final AtomicInteger currentPage = new AtomicInteger();
         StringBuilder pageContents = new StringBuilder();
         doTokens(copyId, feature, (pkg) ->
         {
            try
            {
               int pg = currentPage.get();
               // ended page, flush contents
               if (pg != pkg.pageNum)
               {
                  if (pageContents.length() > 0)
                  {
                     pageDoc.create()
                     .setId(copyId+":"+pg)
                     .setPageSequence(pg)
                     .setPageText(pageContents.toString());

                     pageDocs.add(pageDoc.getDocument());

                     pageContents.delete(0, pageContents.length());
                  }
                  currentPage.set(pkg.pageNum);
               }

               int count = pkg.bodyData.getCount(pkg.token);
               Collections.nCopies(count, pkg.token).stream().forEach(s -> pageContents.append(s).append(" "));

            } catch (Exception e) {
               logger.log(Level.SEVERE, "Error", e);
            }
         });

         String associatedEntry = copyRef.getAssociatedEntry().toString();
         VolumeSolrProxy volDoc = new VolumeSolrProxy();
         StringBuilder volText = new StringBuilder();
         for (SolrInputDocument doc : pageDocs)
         {
            String text = doc.getFieldValue("pageText").toString();
            volText.append(text);
         }
         volDoc.create();
         volDoc.setId(copyId)
               .setAssociatedEntry(associatedEntry)
               .setVolumeText(volText.toString());
         try
         {
            solrPages.add(pageDocs);
            solrPages.commit();
            solrVols.add(volDoc.getDocument());
            solrVols.commit();
         }
         catch(SolrServerException e)
         {
            logger.log(Level.SEVERE, "Failed processing page to solr ["+copyId+"]", e);
         }

      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   private void onUpdate(CopyReference copyRef)
   {

   }

   private void onDelete(CopyReference copyRef)
   {

   }
   static class Pkg
   {
      String token;
      ExtractedFeatures.ExtractedPagePartOfSpeechData bodyData;
      int pageNum;
      ExtractedFeatures.ExtractedPageFeatures page;

      Pkg(String t,
          int pg,
          ExtractedFeatures.ExtractedPageFeatures page,
          ExtractedFeatures.ExtractedPagePartOfSpeechData p)
      {
         token = t;
         pageNum = pg;
         this.page = page;
         bodyData = p;
      }
   }

   private void doTokens(String id, ExtractedFeatures feat, Consumer<Pkg> f) throws Exception
   {

      int pageCount = feat.pageCount();
      for (int pg=0; pg < pageCount; ++pg)
      {
         ExtractedFeatures.ExtractedPageFeatures page = feat.getPage(pg);
         ExtractedFeatures.ExtractedPagePartOfSpeechData bodyData = page.getBodyData();
         Set<String> toks = bodyData.tokens();
         final int fpg = pg;
         toks.stream().sorted().forEach(tok -> f.accept(new Pkg(tok, fpg, page, bodyData)));

      }
   }
}
