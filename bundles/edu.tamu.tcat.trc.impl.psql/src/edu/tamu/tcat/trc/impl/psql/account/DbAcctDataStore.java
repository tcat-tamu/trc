package edu.tamu.tcat.trc.impl.psql.account;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.account.AccountException;
import edu.tamu.tcat.account.login.LoginData;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.auth.account.AccountNotAvailableException;
import edu.tamu.tcat.trc.auth.account.EditTrcAccountCommand;
import edu.tamu.tcat.trc.auth.account.TrcAccount;
import edu.tamu.tcat.trc.auth.account.TrcAccountDataStore;
import edu.tamu.tcat.trc.impl.psql.account.DataModelV1.AccountData;
import edu.tamu.tcat.trc.impl.psql.dbutils.ColumnDefinition;
import edu.tamu.tcat.trc.impl.psql.dbutils.DbTableManager;
import edu.tamu.tcat.trc.impl.psql.dbutils.TableDefinition;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepo;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;

public class DbAcctDataStore implements TrcAccountDataStore
{
   private static final String DATA_COLUMN = "data";
   private final static Logger logger = Logger.getLogger(DbAcctDataStore.class.getName());
   /**
    * UUID for the "guest" account.
    *
    * Note that this UUID does not follow the standard on "version" or "variant", but is all-bits-zero.
    */
   public static final UUID ACCOUNT_ID_GUEST = new UUID(0,0);

   /** Configuration parameter for the database table that will record account data. */
   public static final String PARAM_TABLE_NAME = "trc.accounts.accounts.tablename";

   /** Configuration parameter for the database table that link login identifiers to
    *  account records. */
   public static final String PARAM_LOGINDATA_TABLE_NAME = "trc.accounts.logindata.tablename";

   private static final String ACCOUNT_TABLE_NAME = "accounts";
   private static final String LOGINDATA_TABLE_NAME = "account_login_data";

   private static final String LOGINDATA_ACCOUNT_ID = "account_id";
   private static final String LOGINDATA_PROVIDER_KEY = "login_provider";
   private static final String LOGINDATA_USER_ID = "login_user";

   private PsqlJacksonRepo<TrcAccount, DataModelV1.AccountData, EditTrcAccountCommand> acctRepo;

   private SqlExecutor sqlExecutor;

   private String accountDataTablename;

   private ConfigurationProperties config;

   private String loginDataTablename;

   public void bindSqlExecutor(SqlExecutor exec)
   {
      this.sqlExecutor = exec;
   }

   public void bindConfig(ConfigurationProperties config)
   {
      this.config = config;
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when all dependencies have been provided and the service is ready to run.
    */
   public void activate(Map<String, Object> properties)
   {
      try
      {
         doActivation(properties);
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to activate account data store.", e);
         throw e;
      }
   }

   private void doActivation(Map<String, Object> properties)
   {
      Objects.requireNonNull(sqlExecutor, "SQL executor is not abailable.");
      accountDataTablename = config.getPropertyValue(PARAM_TABLE_NAME, String.class, ACCOUNT_TABLE_NAME);
      loginDataTablename = config.getPropertyValue(PARAM_LOGINDATA_TABLE_NAME, String.class, LOGINDATA_TABLE_NAME);

      acctRepo = buildDocumentRepo();
      initLoginDataTable();
   }

   private PsqlJacksonRepo<TrcAccount, DataModelV1.AccountData, EditTrcAccountCommand> buildDocumentRepo()
   {
      PsqlJacksonRepoBuilder<TrcAccount, DataModelV1.AccountData, EditTrcAccountCommand> repoBuilder =
            new PsqlJacksonRepoBuilder<>();

      repoBuilder.setDbExecutor(sqlExecutor);
      repoBuilder.setPersistenceId(accountDataTablename);
      repoBuilder.setEditCommandFactory(new EditAccountCmdFactory());
      repoBuilder.setDataAdapter(dto -> new DbTrcAccount(dto));
      repoBuilder.setStorageType(DataModelV1.AccountData.class);
      repoBuilder.setEnableCreation(true);

      return repoBuilder.build();
   }

   /**
    * @return The repository schema
    */
   private void initLoginDataTable()
   {
      TableDefinition table = buildLogindataTableDefn();

      DbTableManager manager = new DbTableManager(sqlExecutor);
      if (!manager.exists(table))
      {
         logger.info("Creating database table for account login data bindings. . . ");
         manager.create(table);
      }
   }

   private TableDefinition buildLogindataTableDefn()
   {
      TableDefinition.Builder builder = new TableDefinition.Builder();
      builder.setName(loginDataTablename);

      builder.addColumn(
            new ColumnDefinition.Builder()
                  .setName(LOGINDATA_ACCOUNT_ID)
                  .setType(ColumnDefinition.ColumnType.varchar)
                  .notNull()
                  .build());

      builder.addColumn(
            new ColumnDefinition.Builder()
                  .setName(LOGINDATA_PROVIDER_KEY)
                  .setType(ColumnDefinition.ColumnType.varchar)
                  .notNull()
                  .build());

      builder.addColumn(
            new ColumnDefinition.Builder()
                  .setName(LOGINDATA_USER_ID)
                  .setType(ColumnDefinition.ColumnType.varchar)
                  .notNull()
                  .build());

      TableDefinition table = builder.build();
      return table;
   }

   @Override
   public TrcAccount getAccount(UUID id)
   {
      /*
       * Add logic here for access by guest-accout-id. This way it will work seamlessly with token-based
       * authentication, which uses this API to get an account.
       */
      if (id.equals(ACCOUNT_ID_GUEST))
      {
         DataModelV1.AccountData ad = new DataModelV1.AccountData();
         ad.active = true;
         ad.displayName = "Guest";
         ad.uuid = ACCOUNT_ID_GUEST;
         return new DbTrcAccount(ad);
      }

      return acctRepo.get(id.toString()).orElse(null);
   }

   @Override
   public TrcAccount getAccount(String username) throws AccountException
   {
      String sqlQueryByUsername = "SELECT {0} AS json"
            + " FROM {1} "
            + "WHERE {0}->>''username'' = ? "
              + acctRepo.buildNotRemovedClause();

      String sql = MessageFormat.format(sqlQueryByUsername, DATA_COLUMN, accountDataTablename);
      CompletableFuture<DbTrcAccount> future = sqlExecutor.submit((conn) -> {
         return doLookupByUsername(username, sql, conn);
      });

      String message = "Unexpected internal error trying to retrieve account for username {0}: {1}";
      return unwrap(future, (ex -> MessageFormat.format(message, username, ex.getMessage())));
   }

   public boolean isUsernameAvailable(String username)
   {
      CompletableFuture<Boolean> future = sqlExecutor.submit((conn) -> {
         return isUsernameAvailable(username, conn);
      });

      String message = "Unexpected internal error trying check username {0}: {1}";
      return unwrap(future, (ex -> MessageFormat.format(message, username, ex.getMessage())));
   }

   @Override
   public DbTrcAccount lookup(LoginData data)
   {
      return lookupAccount(data).orElse(null);
   }

   public Optional<DbTrcAccount> lookupAccount(LoginData data)
   {
      CompletableFuture<Optional<DbTrcAccount>> future =
            sqlExecutor.submit(conn -> doAccountLookup(conn, data));

      String msg = "Unexpected internal error looking up account data for login user [{0}] for provider [{1}]";
      return unwrap(future, (ex -> MessageFormat.format(msg, data.getLoginUserId(), data.getLoginProviderId())));
   }

   @Override
   public EditTrcAccountCommand create(LoginData data)
   {
      EditTrcAccountCommand command = acctRepo.create((Account)null);
      ((EditAccountCmdFactory.EditCommand)command).linkLogin(data, this);

      return command;
   }

   @Override
   public EditTrcAccountCommand modify(TrcAccount account, TrcAccount actor)
   {
      return acctRepo.edit(actor, account.getId().toString());
   }

   @Override
   public void link(Account account, LoginData data)
   {
      // NOTE there is a synchronization issue here. This may result in
      //      inconsistent data if the account is removed.
      String accountId = account.getId().toString();
      acctRepo.get(accountId).orElseThrow(() -> new AccountNotAvailableException());
      doLink(account.getId(), data);
   }

   public void revoke(LoginData data)
   {
      String errmsg = "Failed to revoke login data for user {0} for provider [{1}]: {2}";
      unwrap(sqlExecutor.submit(conn -> unlink(conn, data)),
            ex -> MessageFormat.format(errmsg, data.getLoginUserId(), data.getLoginProviderId(), ex.getMessage()));
   }



   /**
    * Internal account linkage. Assumes that the account exists, may throw if it does not.
    *
    * @param accountId
    * @param login
    */
   void doLink(UUID accountId, LoginData login)
   {
      sqlExecutor.submit(conn -> {
         if (isLinked(conn, login))
            updateAccount(conn, login, accountId);
         else
            linkAccount(conn, login, accountId);

         return null;
      });
   }

   private boolean isLinked(Connection conn, LoginData login)
   {
     String sqlTemplate = "SELECT {0} FROM {1} "
                         + "WHERE {2} = ? "
                           + "AND {3} = ?";
     String sql = MessageFormat.format(sqlTemplate,
                                       LOGINDATA_ACCOUNT_ID,
                                       loginDataTablename,
                                       LOGINDATA_PROVIDER_KEY,
                                       LOGINDATA_USER_ID);

     try (PreparedStatement stmt = conn.prepareStatement(sql))
     {
        stmt.setString(1, login.getLoginProviderId());
        stmt.setString(2, login.getLoginUserId());

        try (ResultSet rs = stmt.executeQuery())
        {
           return rs.next();
        }
     }
     catch (SQLException ex)
     {
        throw new IllegalStateException("Failed to determine if login data exists.", ex);
     }
   }

   private boolean unlink(Connection conn, LoginData login)
   {
      String sqlTemplate = "DELETE FROM {0} "
                          + "WHERE {1} = ? "
                            + "AND {2} = ?";
      String sql = MessageFormat.format(sqlTemplate,
                                        loginDataTablename,
                                        LOGINDATA_PROVIDER_KEY,
                                        LOGINDATA_USER_ID);

      try (PreparedStatement stmt = conn.prepareStatement(sql))
      {
         stmt.setString(1, login.getLoginProviderId());
         stmt.setString(2, login.getLoginUserId());

         stmt.executeUpdate();
         return true;
      }
      catch (SQLException ex)
      {
         throw new IllegalStateException("Failed to revoke login link.", ex);
      }
   }

   /**
    * Updates the account id associated with the supplied login credentials.
    *
    * @param conn
    * @param login
    * @param uuid
    */
   private void updateAccount(Connection conn, LoginData login, UUID uuid)
   {
      String sqlTemplate = "UPDATE {0} "
                           + " SET {1} = ? "
                          + "WHERE {2} = ? "
                            + "AND {3} = ?";
      String sql = MessageFormat.format(sqlTemplate,
                                        loginDataTablename,
                                        LOGINDATA_ACCOUNT_ID,
                                        LOGINDATA_PROVIDER_KEY,
                                        LOGINDATA_USER_ID);

      try (PreparedStatement stmt = conn.prepareStatement(sql))
      {
         stmt.setString(1, uuid.toString());
         stmt.setString(2, login.getLoginProviderId());
         stmt.setString(3, login.getLoginUserId());

         stmt.executeUpdate();
      }
      catch (SQLException ex)
      {
         throw new IllegalStateException("Failed to update login if login data exists.", ex);
      }
   }

   private void linkAccount(Connection conn, LoginData login, UUID uuid)
   {
      String sqlTemplate = "INSERT INTO {0} ({1}, {2}, {3}) VALUES (?,?,?)";
      String sql = MessageFormat.format(sqlTemplate,
                                        loginDataTablename,
                                        LOGINDATA_ACCOUNT_ID,
                                        LOGINDATA_PROVIDER_KEY,
                                        LOGINDATA_USER_ID);

      try (PreparedStatement stmt = conn.prepareStatement(sql))
      {
         stmt.setString(1, uuid.toString());
         stmt.setString(2, login.getLoginProviderId());
         stmt.setString(3, login.getLoginUserId());

         stmt.executeUpdate();
      }
      catch (SQLException ex)
      {
         throw new IllegalStateException("Failed to revoke login link.", ex);
      }
   }

   private boolean isUsernameAvailable(String username, Connection conn)
   {
      String sqlTemplate = "SELECT {0} AS json"
            + " FROM {1} "
            + "WHERE {0}->>''username'' = ? "
              + acctRepo.buildNotRemovedClause();

      String sql = MessageFormat.format(sqlTemplate,
                                        DATA_COLUMN,
                                        accountDataTablename);
      try (PreparedStatement stmt = conn.prepareStatement(sql))
      {
         stmt.setString(1, username);

         try (ResultSet rs = stmt.executeQuery())
         {
            return !rs.next();
         }
      }
      catch (SQLException ex)
      {
         throw new IllegalStateException(MessageFormat.format("Internal error checking for username {0}", username), ex);
      }
   }

   private DbTrcAccount doLookupByUsername(String username, String sql, Connection conn)
   {
      String notFound = "No account was found for username {0}";
      try (PreparedStatement stmt = conn.prepareStatement(sql))
      {
         stmt.setString(1, username);

         try (ResultSet rs = stmt.executeQuery())
         {
            if (!rs.next())
               throw new AccountException(MessageFormat.format(notFound, username));

            return parseAccountJson(rs.getString("json"));
         }
      }
      catch (SQLException ex)
      {
         String message = "Failed to restore account from database for username {0}.";
         throw new IllegalStateException(MessageFormat.format(message, username), ex);
      }
   }

   private DbTrcAccount parseAccountJson(String json)
   {
      try {
         ObjectMapper mapper = new ObjectMapper();
         DataModelV1.AccountData dto = mapper.readValue(json, DataModelV1.AccountData.class);

         return new DbTrcAccount(dto);
      }
      catch (IOException ex)
      {
         String message = "Failed to parse account data. JSON Data:\n{0}\n.";
         throw new IllegalStateException(MessageFormat.format(message, json), ex);
      }
   }

   private Optional<DbTrcAccount> doAccountLookup(Connection conn, LoginData data)
   {
      String sqlTemplate = "SELECT acct.{0} AS json"
            + " FROM {1} AS acct LEFT JOIN "
                 + " {2} AS login ON (acct.id = login.account_id)"
            + "WHERE login.{3} = ? "
            + "  AND login.{4} = ? "
              + acctRepo.buildNotRemovedClause();

      String sql = MessageFormat.format(sqlTemplate,
                                        DATA_COLUMN,
                                        accountDataTablename,
                                        this.loginDataTablename,
                                        LOGINDATA_PROVIDER_KEY,
                                        LOGINDATA_USER_ID);

      try (PreparedStatement stmt = conn.prepareStatement(sql))
      {
         stmt.setString(1, data.getLoginProviderId());
         stmt.setString(2, data.getLoginUserId());

         try (ResultSet rs = stmt.executeQuery())
         {
            if (!rs.next())
               return Optional.empty();

            String json = rs.getString("json");
            ObjectMapper mapper = new ObjectMapper();

            AccountData dto = mapper.readValue(json, DataModelV1.AccountData.class);
            DbTrcAccount account = new DbTrcAccount(dto);
            return Optional.of(account);
         }
      }
      catch (SQLException | IOException ex)
      {
         String msg = "Failed to restore account data for user [{0}] from [{1}]";
         throw new IllegalStateException(MessageFormat.format(msg, data.getLoginProviderId(), data.getLoginUserId()), ex);
      }
   }

   private <T> T unwrap(CompletableFuture<T> future, Function<Throwable, String> msg)
   {
      try
      {
         return future.get(10, TimeUnit.SECONDS);
      }
      catch (InterruptedException | TimeoutException e)
      {
         // TODO ideally, we'd throw something more so the REST layer can report a ServerUnavailable. . .
         throw new IllegalStateException("Failed to lookup account information in a timely manner.");
      }
      catch (ExecutionException e)
      {
         Throwable cause = e.getCause();
         if (Error.class.isInstance(cause))
            throw Error.class.cast(cause);

         if (RuntimeException.class.isInstance(cause))
            throw RuntimeException.class.cast(cause);

         throw new IllegalStateException(msg.apply(cause), cause);
      }
   }
}
