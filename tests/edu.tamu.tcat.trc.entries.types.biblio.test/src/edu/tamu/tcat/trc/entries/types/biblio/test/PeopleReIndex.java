package edu.tamu.tcat.trc.entries.types.biblio.test;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import edu.tamu.tcat.catalogentries.CatalogRepoException;
import edu.tamu.tcat.catalogentries.IdFactory;
import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.db.postgresql.exec.PostgreSqlExecutor;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.sda.catalog.idfactory.impl.db.DbBackedObfuscatingIdFactory;
import edu.tamu.tcat.sda.catalog.psql.provider.PsqlDataSourceProvider;
import edu.tamu.tcat.trc.entries.bio.Person;
import edu.tamu.tcat.trc.entries.bio.postgres.PsqlPeopleRepo;
import edu.tamu.tcat.trc.entries.bio.solr.PeopleSolrProxy;

public class PeopleReIndex
{
   private final static Logger logger = Logger.getLogger(PeopleReIndex.class.getName());

   private PostgreSqlExecutor exec;
   private SimpleFileConfigurationProperties config;
   private PsqlDataSourceProvider dsp;
   private PsqlPeopleRepo repo;
   private DbBackedObfuscatingIdFactory factory;

   private SolrServer solr;
   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";
   public static final String SOLR_CORE = "catalogentries.authors.solr.core";

   @Before
   public void setupTest() throws DataSourceException
   {
      Map<String, Object> params = new HashMap<>();
      params.put(SimpleFileConfigurationProperties.PROP_FILE, "config.path");
      config = new SimpleFileConfigurationProperties();
      config.activate(params);

      dsp = new PsqlDataSourceProvider();
      dsp.bind(config);
      dsp.activate();

      exec = new PostgreSqlExecutor();
      exec.init(dsp);

      factory = new DbBackedObfuscatingIdFactory();
      factory.setDatabaseExecutor(exec);
      factory.activate();

      repo = new PsqlPeopleRepo();
      repo.setDatabaseExecutor(exec);
      repo.setIdFactory(factory);
      repo.activate();

      // construct Solr core
      URI solrBaseUri = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);
      String solrCore = config.getPropertyValue(SOLR_CORE, String.class);

      URI coreUri = solrBaseUri.resolve(solrCore);
      logger.info("Connecting to Solr Service [" + coreUri + "]");

      solr = new HttpSolrServer(coreUri.toString());
   }

   @After
   public void tearDownTest() throws InterruptedException, ExecutionException
   {

      repo.dispose();
      exec.close();
      dsp.dispose();
      config.dispose();
      solr.shutdown();
   }

   @Test
   @Ignore
   public void ReIndex()
   {
      try
      {
         solr.deleteByQuery("*:*");
         solr.commit();
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to remove data from the works core.");
      }

      Collection<SolrInputDocument> solrDocs = new ArrayList<>();
      try
      {
         Iterable<Person> people = repo.findPeople();
         for (Person person : people)
         {
            PeopleSolrProxy peopleProxy = PeopleSolrProxy.create(person);
            solrDocs.add(peopleProxy.getDocument());
         }
      }
      catch (CatalogRepoException e)
      {
         logger.log(Level.SEVERE, "Failed to pull people from the db. " + e);
      }

      try
      {
         solr.add(solrDocs);
         solr.commit();
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to commit the authors to the SOLR server. " + e);
      }
   }
}

