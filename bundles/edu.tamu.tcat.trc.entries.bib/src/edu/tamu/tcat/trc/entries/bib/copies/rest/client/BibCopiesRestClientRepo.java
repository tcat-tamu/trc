package edu.tamu.tcat.trc.entries.bib.copies.rest.client;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.bib.copies.CopyReference;
import edu.tamu.tcat.trc.entries.bib.copies.CopyReferenceException;
import edu.tamu.tcat.trc.entries.bib.copies.CopyReferenceRepository;
import edu.tamu.tcat.trc.entries.bib.copies.EditCopyReferenceCommand;
import edu.tamu.tcat.trc.entries.notification.EntryUpdateHelper;
import edu.tamu.tcat.trc.entries.notification.UpdateListener;

public class BibCopiesRestClientRepo implements CopyReferenceRepository
{
   private final static String HACK_HARD_CODED_API_ENDPOINT = "https://neal.citd.tamu.edu/sda/api/catalog/copies/";

   private static final Logger logger = Logger.getLogger(BibCopiesRestClientRepo.class.getName());
   private EntryUpdateHelper<Object> listeners;
   private ObjectMapper mapper;
   private CloseableHttpClient httpclient;

   public void activate()
   {
      httpclient = HttpClients.createDefault();
      listeners = new EntryUpdateHelper<>();

      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   public void dispose()
   {
      try
      {
         httpclient.close();
      }
      catch (IOException e)
      {
         logger.log(Level.SEVERE, "Failed to close HTTP Client", e);
      }

      if (listeners != null)
         listeners.close();

      listeners = null;
      mapper = null;
   }

   @Override
   public EditCopyReferenceCommand create()
   {
      new RestEditCopyRefCommand(null, null, null, null);
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public EditCopyReferenceCommand edit(UUID id) throws NoSuchCatalogRecordException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<CopyReference> getCopies(URI entity)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public CopyReference get(UUID id) throws NoSuchCatalogRecordException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Future<Boolean> remove(UUID id) throws CopyReferenceException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public AutoCloseable register(UpdateListener<CopyReference> ears)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
