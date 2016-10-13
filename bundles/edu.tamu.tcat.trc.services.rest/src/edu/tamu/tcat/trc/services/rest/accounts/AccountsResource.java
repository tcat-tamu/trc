package edu.tamu.tcat.trc.services.rest.accounts;

import java.sql.Connection;
import java.sql.Statement;
import java.text.MessageFormat;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import edu.tamu.tcat.account.db.login.AccountRecord;
//import edu.tamu.tcat.account.apacheds.LdapHelperAdFactory;
//import edu.tamu.tcat.account.apacheds.LdapHelperReader;
//import edu.tamu.tcat.account.apacheds.ad.login.LdapLoginProvider;
import edu.tamu.tcat.account.jaxrs.bean.ContextBean;
import edu.tamu.tcat.account.jaxrs.bean.TokenProviding;
import edu.tamu.tcat.account.login.LoginData;
import edu.tamu.tcat.account.login.LoginProvider;
import edu.tamu.tcat.crypto.CryptoProvider;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.auth.account.EditTrcAccountCommand;
import edu.tamu.tcat.trc.auth.account.TrcAccount;
import edu.tamu.tcat.trc.auth.account.TrcAccountDataStore;
import edu.tamu.tcat.trc.impl.psql.account.DatabaseAuthnManager;
import edu.tamu.tcat.trc.impl.psql.account.DatabaseLoginProvider;
import edu.tamu.tcat.trc.impl.psql.account.DbAcctDataStore;
import edu.tamu.tcat.trc.services.rest.accounts.v1.RestApiV1.LoginRequestDTO;

@Path("/accounts")
public class AccountsResource
{
   private static final String REST_TOKEN_PWDRESET_KEY = "edu.tamu.tcat.sda.rest.token.pwdreset.key";
   private static final Logger debug = Logger.getLogger(AccountsResource.class.getName());
   private static final String DEFAULT_LOGIN_PROVIDER = "loginprovider.db.sda";

   private ConfigurationProperties svcConfig;
   private TrcAccountDataStore svcAccountStore;
   private CryptoProvider svcCrypto;
   private SqlExecutor svcExec;

   void bind(TrcAccountDataStore accounts)
   {
      svcAccountStore = accounts;
   }

   void bind(ConfigurationProperties svc)
   {
      svcConfig = svc;
   }

   // Don't actually need the value, but bind to ensure the feature has been installed
   // before this resource loads. Otherwise there is a race condition between the two declarative
   // services that could cause this to fail to load
   void bind(MultiPartFeature svc)
   {
   }

   void bind(SqlExecutor svc)
   {
      svcExec = svc;
   }

   void bind(CryptoProvider svc)
   {
      svcCrypto = svc;
   }

   void activate()
   {
   }

   @GET
   public String doGet()
   {
      return "Get";
   }

   /**
    * Authenticate using parameters sent via "application/x-www-form-urlencoded"
    */
   @POST
   @Path ("/auth_ajax")
   @Produces(MediaType.APPLICATION_JSON)
   @TokenProviding(payloadType=TrcAccount.class)
   public Map<String,Object> authAjax(@BeanParam ContextBean bean,
                                      @FormParam("username") String username,
                                      @FormParam("password") String password,
                                      @FormParam("provider") @DefaultValue(DEFAULT_LOGIN_PROVIDER) String providerId)
   {
      return auth(bean, username, password, providerId);
   }

   /**
    * Authenticate using parameters sent via "multipart/form-data"
    */
   @POST
   @Path("/auth_form")
   @Produces(MediaType.APPLICATION_JSON)
   @TokenProviding(payloadType=TrcAccount.class)
   public Map<String,Object> authForm(@BeanParam ContextBean bean,
                                      @FormDataParam("username") String username,
                                      @FormDataParam("password") String password,
                                      @FormDataParam("provider") @DefaultValue(DEFAULT_LOGIN_PROVIDER) String providerId)
   {
      return auth(bean, username, password, providerId);
   }

   /**
    * Authenticate using parameters sent via "application/json"
    */
   @POST
   @Path("/auth_json")
   @Produces(MediaType.APPLICATION_JSON)
   @TokenProviding(payloadType=TrcAccount.class)
   public Map<String,Object> authJson(@BeanParam ContextBean bean,
                                      LoginRequestDTO postdata)
   {
      if (postdata.providerId == null)
         postdata.providerId = DEFAULT_LOGIN_PROVIDER;
      return auth(bean, postdata.username, postdata.password, postdata.providerId);
   }

   /**
    * Authenticate using guest account (no params)
    */
   @POST
   @Path("/authguest")
   @Produces(MediaType.APPLICATION_JSON)
   @TokenProviding(payloadType=TrcAccount.class)
   public Map<String,Object> authGuestJson(@BeanParam ContextBean bean)
   {
      try
      {
         TrcAccount account = svcAccountStore.getAccount(DbAcctDataStore.ACCOUNT_ID_GUEST);
         bean.set(account, TrcAccount.class);
         Map<String,Object> rv = new HashMap<>();
         rv.put("uuid", account.getId());
         return rv;
      }
      catch (Exception ex)
      {
         debug.log(Level.FINE, "Failed auth", ex);
         throw new ForbiddenException(ex);
      }
   }

   /**
    * Authenticate using parameters sent via HTTP query parameters
    */
   @POST
   @Path("/auth_query")
   @Produces(MediaType.APPLICATION_JSON)
   @TokenProviding(payloadType=TrcAccount.class)
   public Map<String,Object> authQuery(@BeanParam ContextBean bean,
                                       @QueryParam("username") String username,
                                       @QueryParam("password") String password,
                                       @QueryParam("provider") @DefaultValue(DEFAULT_LOGIN_PROVIDER) String providerId)
   {
      return auth(bean, username, password, providerId);
   }

   private Map<String,Object> auth(ContextBean bean, String username, String password, String providerId)
   {
      if (username == null || username.length() == 0)
         throw new BadRequestException("Username not specified");
      if (password == null || password.length() == 0)
         throw new BadRequestException("Password not specified");

      //TODO: later, allow the user to select a Login Provider
      //String providerId = LOGIN_PROVIDER_DB;

      LoginProvider loginProvider = null;
      if (providerId.equals(DEFAULT_LOGIN_PROVIDER))
      {
         try
         {
            loginProvider = getDbLoginProvider(username, password);
         }
         catch (Exception e)
         {
            throw new InternalServerErrorException("Failed initializing login provider ["+providerId+"]", e);
         }
      }
      else
      {
         throw new BadRequestException("Unknown login provider ["+providerId+"]");
      }

      try
      {
         // provider encapsulates everything, so try to log in (or fail)
         LoginData data = loginProvider.login();
         if (data == null)
            throw new IllegalArgumentException("Authentication failed");
         TrcAccount account = null;
         try
         {
            account = svcAccountStore.lookup(data);
         }
         catch (Exception e)
         {
            debug.log(Level.WARNING, "Error occurred during account lookup", e);
         }

         if (account == null)
         {
            // failed lookup, try creating the account and linking to LoginData
            //FIXME: this does not properly populate the new account with login data values
            EditTrcAccountCommand cmd = svcAccountStore.create(data);
            CompletableFuture<TrcAccount> future = cmd.execute();
            account = future.get(2, TimeUnit.MINUTES);
         }

         bean.set(account, TrcAccount.class);
         Map<String,Object> rv = new HashMap<>();
         rv.put("uuid", account.getId());
         return rv;
      }
      catch (Exception ex)
      {
         debug.log(Level.FINE, "Failed auth", ex);
         throw new ForbiddenException(ex);
      }
   }

   private LoginProvider getDbLoginProvider(String username, String password) throws Exception
   {
      CompletableFuture<Object> future = svcExec.submit(new SqlExecutor.ExecutorTask<Object>()
      {
         @Override
         public Object execute(Connection conn) throws Exception
         {
            //HACK: children, don't try this at home. Hijacking the "create table" sql with a string-replace in it to inject the 'IF NOT EXISTS' clause
            String sql = MessageFormat.format(DatabaseAuthnManager.CREATE_DB_TABLE, "IF NOT EXISTS " + DatabaseAuthnManager.SQL_TABLENAME);
            try (Statement stmt = conn.createStatement())
            {
               debug.info("Invoking: " + sql);
               stmt.execute(sql);
            }
            return null;
         }
      });
      future.get(2, TimeUnit.MINUTES);

      DatabaseLoginProvider rv = new DatabaseLoginProvider();
      rv.init(DEFAULT_LOGIN_PROVIDER, username, password, getAuthManager());
      return rv;
   }

   private DatabaseAuthnManager getAuthManager()
   {
      DatabaseAuthnManager authManager = DatabaseAuthnManager.instantiate(svcConfig, svcExec, svcCrypto, REST_TOKEN_PWDRESET_KEY, 24, ChronoUnit.HOURS);
      return authManager;
   }

   @POST
   @Path("/create_json")
   @Produces(MediaType.APPLICATION_JSON)
   public Map<String,Object> createJson(LoginRequestDTO postdata)
   {
      if (postdata.username == null || postdata.username.length() == 0)
         throw new BadRequestException("Username not specified");
      if (postdata.password == null || postdata.password.length() == 0)
         throw new BadRequestException("Password not specified");


      AccountRecord record = new AccountRecord();
      record.email = "eml";
      record.first = "fn";
      record.last = "ln";
      record.username = postdata.username;
      AccountRecord updated = getAuthManager().createRecord(record, postdata.password);

      Map<String,Object> rv = new HashMap<>();
      rv.put("uid", Long.valueOf(updated.uid));
      return rv;
   }

   //TODO: for potential future use in LDAP authN
//   static class LdapConfig
//   {
//      public String host;
//      public Integer port;
//      public String adminUserDN;
//      public String adminPassword;
//      public Boolean ssl;
//      public Boolean tls;
//      public List<String> searchOUList;
//   }
//   private static final String PROP_LDAP_CONFIG = "edu.tamu.tcat.datasvc.accounts.ldap.config.file";
//
//   /**
//    * This is the name of an LDAP group to which accounts must belong or authentication is denied.
//    */
//   private static final String APP_ID_GROUPNAME = "Data-Service";
//   private LoginProvider getLdapLoginProvider(String username, String password)
//   {
//      java.nio.file.Path filePath = svcConfig.getPropertyValue(PROP_LDAP_CONFIG, java.nio.file.Path.class);
//      LdapConfig cfg = null;
//      try (InputStream input = Files.newInputStream(filePath))
//      {
//         ObjectMapper mapper = new ObjectMapper();
//         cfg = mapper.readValue(input, LdapConfig.class);
//      }
//      catch (Exception e)
//      {
//         throw new IllegalStateException("Failed parsing LDAP config ["+filePath+"]", e);
//      }
//
//      if (cfg.port == null || cfg.port.intValue() <= 0)
//         throw new IllegalStateException("Missing 'port' configuration for LDAP");
//
//      if (cfg.ssl == null)
//         cfg.ssl = Boolean.FALSE;
//      if (cfg.tls == null)
//         cfg.tls = Boolean.FALSE;
//
//      if (cfg.searchOUList == null || cfg.searchOUList.isEmpty())
//         throw new IllegalStateException("Missing 'searchOUList' configuration for LDAP");
//
//      LdapHelperReader ldapReader = new LdapHelperAdFactory().buildReader(cfg.host,
//            cfg.port.intValue(), cfg.adminUserDN, cfg.adminPassword, cfg.ssl.booleanValue(), cfg.tls.booleanValue(), cfg.searchOUList.get(0));
//
//      LdapLoginProvider rv = new LdapLoginProvider();
//      rv.init(ldapReader, username, password, LdapLoginProvider.PROVIDER_ID, cfg.searchOUList);
//      rv.setRequiredGroup(APP_ID_GROUPNAME);
//      return rv;
//   }

   //TODO: for future use in getting AuthZ information
//   @GET
//   @Path("/data/{acctId}/roles")
//   @Produces(MediaType.APPLICATION_JSON)
//   @TokenSecured(payloadType=Account.class)
//   public RestApiV1.AccountDTO getRoles(@BeanParam ContextBean context,
//                                        @PathParam("acctId") String accountId)
//   {
//      Account account = context.get(Account.class);
//      UUID reqId = UUID.fromString(accountId);
//      // If the secured requestor is asking about their own account, then allow it
//      if (!account.getId().equals(reqId))
//      {
//         //TODO: asking about a different account requires some privileges
//         //if (!svcAccountStoreEx.hasRole(account, ""))
//         //   return Response.status(Response.Status.FORBIDDEN).build();
//         throw new ForbiddenException();
//      }
//
//      RestApiV1.AccountDTO dto = new RestApiV1.AccountDTO();
//      dto.uuid = reqId;
//      Account reqAcct = svcAccountStoreEx.getAccount(reqId);
//      if (reqAcct == null)
//         throw new ForbiddenException();
//      dto.roles = svcAccountStoreEx.getRoles(reqAcct);
//      return dto;
//   }
}
