package edu.tamu.tcat.trc.refman.postgres;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.refman.CollectionDescriptor;
import edu.tamu.tcat.trc.refman.ReferenceCollection;
import edu.tamu.tcat.trc.refman.ReferenceCollectionManager;

public class PsqlRepository
{
   private final Map<String, ReferenceCollectionManager> managers = new HashMap<>();

   private SqlExecutor exec;
   private ConfigurationProperties config;

   public PsqlRepository()
   {
      // TODO Auto-generated constructor stub
   }


   public void addCollectionManager(ReferenceCollectionManager manager)
   {
      managers.put(manager.getId(), manager);
   }
   public void setConfiguration(ConfigurationProperties config)
   {
      // H
      this.config = config;
   }

   public void setSqlExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public class AccountImpl implements Account {

      @Override
      public UUID getId()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public String getTitle()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public boolean isActive()
      {
         return true;
      }

   }

   public void activate()
   {
      // HACK -- example only - use this as template for testing if needed.
      Map<String, String> params = new HashMap<String, String>();
      params.put("id", "local");
      params.put("title", "Local Bibliographic References");

      LocalCollectionManager localCollections = new LocalCollectionManager();
      localCollections.setConfiguration(config);
      localCollections.setSqlExecutor(exec);
      localCollections.activate(params);

      managers.put(localCollections.getId(), localCollections);
   }

   public List<CollectionDescriptor> listCollections(Account account)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public ReferenceCollection get(Account account, CollectionDescriptor collectionId)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Collection<ReferenceCollectionManager> listCollectionScopes(Account account)
   {
      // TODO Auto-generated method stub
      return null;
   }
}
