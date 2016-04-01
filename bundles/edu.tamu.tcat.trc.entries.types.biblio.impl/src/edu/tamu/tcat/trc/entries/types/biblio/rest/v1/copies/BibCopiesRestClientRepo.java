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
package edu.tamu.tcat.trc.entries.types.biblio.rest.v1.copies;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.logging.Logger;

//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.trc.entries.notification.EntryUpdateHelper;
import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyChangeEvent;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceException;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceRepository;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.EditCopyReferenceCommand;

public class BibCopiesRestClientRepo implements CopyReferenceRepository
{
   private final static String HACK_HARD_CODED_API_ENDPOINT = "https://neal.citd.tamu.edu/sda/api/catalog/copies/";

   private static final Logger logger = Logger.getLogger(BibCopiesRestClientRepo.class.getName());
   private EntryUpdateHelper<UpdateEvent> listeners;
   private ObjectMapper mapper;
//   private CloseableHttpClient httpclient;

   public void activate()
   {
//      httpclient = HttpClients.createDefault();
      listeners = new EntryUpdateHelper<>();

      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   public void dispose()
   {
//      try
//      {
//         httpclient.close();
//      }
//      catch (IOException e)
//      {
//         logger.log(Level.SEVERE, "Failed to close HTTP Client", e);
//      }

      if (listeners != null)
         listeners.close();

      listeners = null;
      mapper = null;
   }

   @Override
   public EditCopyReferenceCommand create()
   {
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
   public List<CopyReference> getCopies(URI entity, boolean deep)
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
   public AutoCloseable register(UpdateListener<CopyChangeEvent> ears)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
