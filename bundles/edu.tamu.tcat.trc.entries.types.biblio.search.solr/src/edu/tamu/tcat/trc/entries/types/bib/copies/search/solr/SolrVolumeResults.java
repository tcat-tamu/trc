package edu.tamu.tcat.trc.entries.types.bib.copies.search.solr;

import java.util.List;

import edu.tamu.tcat.trc.entries.types.biblio.copies.search.VolumeSearchCommand;
import edu.tamu.tcat.trc.entries.types.biblio.copies.search.VolumeSearchProxy;
import edu.tamu.tcat.trc.entries.types.biblio.copies.search.VolumeSearchResult;

public class SolrVolumeResults implements VolumeSearchResult
{
   private VolumeSolrSearchCommand cmd;
   private List<VolumeSearchProxy> vols;

   public SolrVolumeResults(VolumeSolrSearchCommand cmd, List<VolumeSearchProxy> vols)
   {
      this.cmd = cmd;
      this.vols = vols;
   }

   @Override
   public VolumeSearchCommand getCommand()
   {
      return cmd;
   }

   @Override
   public List<VolumeSearchProxy> get()
   {
      return vols;
   }
}
