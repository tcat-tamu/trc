package edu.tamu.tcat.trc.entries.types.bib.copies.search.solr;

import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import edu.tamu.tcat.trc.entries.types.biblio.copies.search.VolumeSearchCommand;
import edu.tamu.tcat.trc.entries.types.biblio.copies.search.VolumeSearchProxy;
import edu.tamu.tcat.trc.entries.types.biblio.copies.search.VolumeSearchResult;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;

public class VolumeSolrSearchCommand implements VolumeSearchCommand
{
   private static final int DEFAULT_MAX_RESULTS = 25;

   private SolrServer solr;
   private TrcQueryBuilder qb;

   public VolumeSolrSearchCommand(SolrServer solrVols, TrcQueryBuilder trcQueryBuilder)
   {
      this.solr = solrVols;
      this.qb = trcQueryBuilder;
      this.qb.max(DEFAULT_MAX_RESULTS);
   }

   @Override
   public VolumeSearchResult execute() throws SearchException
   {
      try
      {
         QueryResponse response = solr.query(qb.get());
         SolrDocumentList results = response.getResults();
         List<VolumeSearchProxy> vols = qb.unpack(results, VolumeSolrSearchCommand::proxyAdapter);
         return new SolrVolumeResults(this, vols);
      }
      catch (SolrServerException e)
      {
         throw new SearchException("An error occurred while querying the volume core", e);
      }
   }

   private static VolumeSearchProxy proxyAdapter(SolrDocument doc)
   {
      VolumeSearchProxy proxy = new VolumeSearchProxy();
      proxy.id = doc.getFieldValue("volumeText").toString();
      return proxy;
   }

   @Override
   public void query(String basicQueryString) throws SearchException
   {
      qb.basic(basicQueryString);
   }

   @Override
   public void setOffset(int offset)
   {
      if (offset < 0)
         throw new IllegalArgumentException("Offset [" + offset + "] cannot be negative.");
      qb.offset(offset);
   }

   @Override
   public void setMaxResults(int count)
   {
      qb.max(count);
   }

}
