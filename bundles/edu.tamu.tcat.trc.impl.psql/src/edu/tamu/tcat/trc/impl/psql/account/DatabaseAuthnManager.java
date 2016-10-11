/*
 * Copyright 2014 Texas A&M Engineering Experiment Station
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
package edu.tamu.tcat.trc.impl.psql.account;

import static java.text.MessageFormat.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import edu.tamu.tcat.account.AccountException;
import edu.tamu.tcat.account.db.AdaptingTokenService;
import edu.tamu.tcat.account.db.ExpiringTokenProvider.LongTokenProviderFactory;
import edu.tamu.tcat.account.db.login.AccountRecord;
import edu.tamu.tcat.account.login.LoginProvider;
import edu.tamu.tcat.account.token.TokenService;
import edu.tamu.tcat.account.token.TokenService.TokenData;
import edu.tamu.tcat.crypto.CryptoProvider;
import edu.tamu.tcat.crypto.DigestType;
import edu.tamu.tcat.crypto.PBKDF2;
import edu.tamu.tcat.crypto.SecureToken;
import edu.tamu.tcat.crypto.TokenException;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;

/**
 * Manages username/password credentials and associated basic account management information.
 *
 * @deprecated Copied from e.t.t.account.db.login and modified to fix defects
 */
@Deprecated
public final class DatabaseAuthnManager
{
   private static final Logger logger = Logger.getLogger(DatabaseAuthnManager.class.getName());


   private static final String DEFAULT_TOKEN_PROP_KEY = "authn.password.token";

   public static final String PROP_TOKEN_PROPERTY_KEY = "tokenPropertyKey";
   public static final String PROP_TOKEN_EXPIRES_KEY = "tokenExpires";
   public static final String PROP_TOKEN_EXPIRES_UNIT_KEY = "tokenExpiresUnit";

   //TODO: where to declare these keys? Can't be here, since this is internal.
   //      Could go in some e.t.t.a.db.LoginDataKeys, perhaps?
   /** Named key to request a value from {@link DbLoginData} type: Long  */
   public static final String DATA_KEY_UID = "user_id";
   /** Named key to request a value from {@link DbLoginData} type: String */
   public static final String DATA_KEY_USERNAME = "username";
   /** Named key to request a value from {@link DbLoginData} type: String */
   public static final String DATA_KEY_FIRST = "first";
   /** Named key to request a value from {@link DbLoginData} type: String */
   public static final String DATA_KEY_LAST = "last";
   /** Named key to request a value from {@link DbLoginData} type: String */
   public static final String DATA_KEY_EMAIL = "email";

   public static final String CREATE_DB_TABLE =
             "CREATE TABLE {0} (" +
             "  user_id BIGSERIAL NOT NULL, " +
             "  user_name VARCHAR NOT NULL," +
             "  password_hash VARCHAR," +
             "  reset_hash VARCHAR DEFAULT NULL," +
             "  first_name VARCHAR," +
             "  last_name VARCHAR," +
             "  email VARCHAR" +
             ")";
   public static final String CREATE_ACCOUNT_SQL = "INSERT INTO {0} (user_name, password_hash, first_name, last_name, email) VALUES (?,?,?,?,?)";
   public static final String SET_RESET_HASH =
         "UPDATE {0}" +
           " SET password_hash = null," +
               " reset_hash = ?" +
         " WHERE user_name = ? ";
   public static final String GET_ACCOUNT_TEMPLATE = "SELECT * FROM {0} WHERE {1} = ?";

   public static final String SQL_TABLENAME = "authn_local";
   private static final String SQL_COL_USERNAME = "user_name";
   private static final String SQL_COL_PWDHASHED = "password_hash";

   private CryptoProvider cp;
   private SqlExecutor exec;
   private ConfigurationProperties config;

   private AccountRecordTokenService accountTokens;

   public static DatabaseAuthnManager instantiate(ConfigurationProperties config, SqlExecutor exec, CryptoProvider crypto)
   {
      return instantiate(config, exec, crypto, DEFAULT_TOKEN_PROP_KEY, 24, ChronoUnit.HOURS);
   }

   public static DatabaseAuthnManager instantiate(ConfigurationProperties config, SqlExecutor exec, CryptoProvider crypto,
                                                  String tokenKey, long duration, ChronoUnit unit)
   {
      DatabaseAuthnManager mgr = new DatabaseAuthnManager();
      mgr.bind(config);
      mgr.bind(exec);
      mgr.bind(crypto);

      Map<String, Object> props = new HashMap<>();
      props.put(DatabaseAuthnManager.PROP_TOKEN_PROPERTY_KEY, tokenKey);
      props.put(DatabaseAuthnManager.PROP_TOKEN_EXPIRES_KEY, Long.valueOf(duration));
      props.put(DatabaseAuthnManager.PROP_TOKEN_EXPIRES_UNIT_KEY, unit.name());
      mgr.activate(props);

      return mgr;
   }

//   CREATE TABLE local_logins (
//     uid BIGSERIAL NOT NULL,
//     user_name VARCHAR NOT NULL,
//     password_hash BYTEA,
//     reset_hash BYTEA DEFAULT NULL,
//     first_name VARCHAR,
//     last_name VARCHAR,
//     email VARCHAR
//   )

   public DatabaseAuthnManager()
   {
   }

   public void bind(ConfigurationProperties properties)
   {
      this.config = properties;
   }

   public void bind(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void bind(CryptoProvider cryptoProvider)
   {
      this.cp = cryptoProvider;
   }

   /**
    * This supports configuration via three properties.
    *
    * <ul>
    *   <li>{@link DatabaseAuthnManager#PROP_TOKEN_PROPERTY_KEY} - Defines the property key in the
    *       configuration file to be used . Defaults to {@code authn.password.token}.</li>
    *   <li>{@link DatabaseAuthnManager#PROP_TOKEN_EXPIRES_KEY} - The amount of time password
    *       reset tokens should be valid before they can no longer be used. Defaults to {@code 24}.</li>
    *   <li>{@link DatabaseAuthnManager#PROP_TOKEN_EXPIRES_UNIT_KEY} - The time unit associated
    *       with the token expiration time. Must be a String representation of a value defined
    *       by {@link ChronoUnit}. Defaults to {@link ChronoUnit#HOURS}.</li>
    * </ul>
    *
    * @param properties
    */
   public void activate(Map<String, Object> properties)
   {
      // TODO add config props for:
      //      - fail on missing password
      //      - password strength criteria
      //      - enable DB creation
      //      - token expiration
      try
      {
         long expires = 24;
         ChronoUnit expireUnit = ChronoUnit.HOURS;

         try {
            // try to set the expiration and units from provided map. if not present, fallback to defaults
            String u = (String)properties.get(PROP_TOKEN_EXPIRES_UNIT_KEY);
            if (u != null) {
               expireUnit = ChronoUnit.valueOf(u);
            }

            Long e = (Long)properties.get(PROP_TOKEN_EXPIRES_KEY);
            expires = e.longValue();
         } catch (Exception ex) {
            logger.log(Level.WARNING, "Invalid token expiration configuration.", ex);
            expires = 24;
            expireUnit = ChronoUnit.HOURS;
         }

         String p = (String)properties.get(PROP_TOKEN_PROPERTY_KEY);
         String propertyKey = Objects.requireNonNull(p, format("[{0}] must be set", PROP_TOKEN_PROPERTY_KEY));
         SecureToken secureToken = decodeToken(propertyKey);

         TokenService<Long> longTokenService = LongTokenProviderFactory.makeProvider(secureToken, expires, expireUnit);
         accountTokens = new AccountRecordTokenService(longTokenService, this);
      }
      catch (Exception ex)
      {
         String msg = "Failed to start database authentication manager.";
         logger.log(Level.SEVERE, msg, ex);
         if (ex instanceof RuntimeException)
            throw (RuntimeException)ex;

         throw new IllegalStateException(msg, ex);
      }
   }

   private SecureToken decodeToken(String propertyKey)
   {
      propertyKey = propertyKey != null && !propertyKey.trim().isEmpty() ? propertyKey : DEFAULT_TOKEN_PROP_KEY;
      String keyString = config.getPropertyValue(propertyKey, String.class);
      if (keyString == null || keyString.length() == 0)
         throw new IllegalStateException(format("No token key provided for property [{0}]", propertyKey));

      byte[] key;
      try
      {
         key = Base64.getDecoder().decode(keyString);
      }
      catch (Exception e)
      {
         throw new IllegalStateException(format("Could not decode token key in property [{0}]", propertyKey), e);
      }
      try
      {
         return cp.getSecureToken(key);
      }
      catch (TokenException e)
      {
         throw new IllegalStateException(format("Could not create secure token for key in property [{0}]", propertyKey), e);
      }
   }

   public LoginProvider createLoginProvider(String username, String passwordRaw)
   {
      DatabaseLoginProvider provider = new DatabaseLoginProvider();
      provider.init("db.basic", username, passwordRaw, this);

      return provider;

   }
   /**
    * Creates a new account login record within the database.
    *
    * @param data The account data to be created.
    * @param passwordRaw The plain text password to use when authenticating this account. This
    *       will not be stored directly. Will be salted and hashed using the PBKDF2 algorithm.
    *
    * @return
    * @throws Exception
    */
   public AccountRecord createRecord(AccountRecord data, String passwordRaw) throws AccountException
   {
      // TODO validate password complexity.
      SqlExecutor.ExecutorTask<AccountRecord> task = new CreateAccountRecordTask(data, deriveHash(passwordRaw));
      Future<AccountRecord> f = exec.submit(task);
      return unwrap(f, () -> format("Failed create account login data for user [{0}]", data.username));
   }

   /**
    * Retrieves an account record from the database, verifying the the supplied password
    * matches the stored value.
    *
    * @param name The username of the account to check.
    * @param passwordRaw The password of the account to check
    * @return The associated account information.
    *
    * @throws AccountException
    */
   public AccountRecord authenticate(String name, String passwordRaw) throws AccountException
   {
      SqlExecutor.ExecutorTask<AccountRecord> task = new GetAccountRecordTask(name, passwordRaw);
      Future<AccountRecord> f = exec.submit(task);
      return unwrap(f, () -> format("Failed get account login data for user [{0}]", name));
   }

   public AccountRecord getRecord(long userId) throws AccountException
   {
      SqlExecutor.ExecutorTask<AccountRecord> task = new GetRecordByIdTask(userId);
      Future<AccountRecord> f = exec.submit(task);
      return unwrap(f, () -> format("Failed get account login data for user [{0}]", userId));
   }

   public void updateRecord()
   {
   }

   public void deactive(String username)
   {
      throw new UnsupportedOperationException();
   }

   public void lock(String username)
   {
      throw new UnsupportedOperationException();
   }

   public void lock(String username, int time, TimeUnit units)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Creates a secure, expiring token that will allow a users password to be set.
    *
    * @param username
    * @return
    * @throws AccountException
    */
   public TokenData<AccountRecord> makeResetToken(String username) throws AccountException
   {
      if (username == null || username.length() == 0)
         throw new IllegalArgumentException("Username not specified\n");

      SqlExecutor.ExecutorTask<TokenData<AccountRecord>> task = new CreateResetTokenTask(username);
      Future<TokenData<AccountRecord>> f = exec.submit(task);
      return unwrap(f, () -> format("Failed get reset password for user [{0}]", username));
   }

   public void resetPassword(String resetToken, String rawPassword) throws AccountException
   {
      AccountRecord record = accountTokens.unpackToken(resetToken);

      SqlExecutor.ExecutorTask<Void> task = new ResetPasswordTokenTask(record, resetToken, deriveHash(rawPassword));
      Future<Void> f = exec.submit(task);
      unwrap(f, () -> format("Failed reset password for user [{0}] using token [{1}]", record.username, resetToken));
   }

   public void setPassword(String username, String oldPasswordRaw, String newPasswordRaw) throws AccountException
   {
      SqlExecutor.ExecutorTask<Void> task = new AuthenticatedPasswordResetTask(username, oldPasswordRaw, deriveHash(newPasswordRaw));
      Future<Void> f = exec.submit(task);
      unwrap(f, () -> format("Failed reset password for user [{0}]", username));
   }

   private <T> T unwrap(Future<T> future, Supplier<String> error) throws AccountException
   {
      try
      {
         return future.get(10, TimeUnit.SECONDS);
      }
      catch (InterruptedException | TimeoutException e)
      {
         throw new AccountException(error.get(), e);
      }
      catch (ExecutionException ex)
      {
         Throwable cause = ex.getCause();
         if (AccountException.class.isInstance(cause))
            throw (AccountException)cause;
         if (LoginException.class.isInstance(cause))
            throw new AccountException(cause.getMessage(), cause);

         throw new AccountException(error.get(), cause);
      }
   }

   private boolean authenticate(String username, String passwordRaw, String passwordHashed)
   {
      if (passwordHashed == null)
      {
         logger.warning(format("User [{0}] has no stored credentails.", username));
         return false;
      }

      PBKDF2 pbkdf2Impl = cp.getPbkdf2(DigestType.SHA1);
      return pbkdf2Impl.checkHash(passwordRaw, passwordHashed);
   }

   private String deriveHash(String passwordRaw)
   {
      try
      {
         PBKDF2 pbkdf2Impl = cp.getPbkdf2(DigestType.SHA1);
         return pbkdf2Impl.deriveHash(passwordRaw);
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed password hash derivation", e);
      }
   }

   private AccountRecord getRecordFromDb(Connection conn, String username, String rawPassword)
         throws AccountNotFoundException, SQLException, FailedLoginException
   {
      String sql = format(GET_ACCOUNT_TEMPLATE,  SQL_TABLENAME, SQL_COL_USERNAME);
      try (PreparedStatement ps = conn.prepareStatement(sql))
      {
         ps.setString(1, username);
         try (ResultSet rs = ps.executeQuery())
         {
            if (!rs.next())
               throw new AccountNotFoundException(format("No user exists with name '{0}'", username));

            String storedHash = rs.getString(SQL_COL_PWDHASHED);
            boolean passed = authenticate(username, rawPassword, storedHash);
            if (!passed)
               throw new FailedLoginException("password incorrect");

            AccountRecord rv = new AccountRecord();
            rv.uid = rs.getLong("user_id");
            rv.username = username;
            rv.first = rs.getString("first_name");
            rv.last = rs.getString("last_name");
            rv.email = rs.getString("email");

            return rv;
         }
      }
   }

   private static class AccountRecordTokenService extends AdaptingTokenService<AccountRecord, Long>
   {
      public AccountRecordTokenService(TokenService<Long> delegate, DatabaseAuthnManager accounts)
      {
         super(AccountRecord.class, delegate, rv -> rv.uid, accounts::getRecord);
      }

   }

   /**
    * Performs token-based password reset.
    */
   private class ResetPasswordTokenTask implements SqlExecutor.ExecutorTask<Void>
   {
      public static final String RESET_PASSWORD =
            "UPDATE {0}" +
              " SET password_hash = ?," +
                  " reset_hash = null" +
            " WHERE user_id = ? AND reset_hash = ?";

      private long uId;
      private String token;
      private String hashedPass;

      public ResetPasswordTokenTask(AccountRecord record, String token, String hashedPass)
      {
         this.uId = record.uid;
         this.token = token;
         this.hashedPass = hashedPass;
      }

      @Override
      public Void execute(Connection conn) throws Exception
      {
         String sql = format(RESET_PASSWORD, SQL_TABLENAME);
         try (PreparedStatement ps = conn.prepareStatement(sql))
         {
            ps.setString(1, hashedPass);
            ps.setLong(2, this.uId);
            ps.setString(3, this.token);

            int ct = ps.executeUpdate();
            if (ct != 1)
               throw new AccountException(format("Failed to reset password. {0} rows updated.", ct));

            return null;
         }
      }
   }

   /**
    * Performs token-based password reset.
    */
   private class AuthenticatedPasswordResetTask implements SqlExecutor.ExecutorTask<Void>
   {
      public static final String RESET_PASSWORD =
            "UPDATE {0}" +
              " SET password_hash = ?" +
            " WHERE user_name = ?";

      private String username;
      private String oldPasswordRaw;
      private String hashedPassword;

      public AuthenticatedPasswordResetTask(String username, String oldPasswordRaw, String hashedPassword)
      {
         this.username = username;
         this.oldPasswordRaw = oldPasswordRaw;
         this.hashedPassword = hashedPassword;
      }

      @Override
      public Void execute(Connection conn) throws Exception
      {
         getRecordFromDb(conn, username, oldPasswordRaw);
         String sql = format(RESET_PASSWORD, SQL_TABLENAME);
         try (PreparedStatement ps = conn.prepareStatement(sql))
         {
            ps.setString(1, hashedPassword);
            ps.setString(2, this.username);

            int ct = ps.executeUpdate();
            if (ct != 1)
               throw new AccountException(format("Failed to reset password. {0} rows updated.", ct));

            return null;
         }
      }
   }

   private class CreateResetTokenTask implements SqlExecutor.ExecutorTask<TokenData<AccountRecord>>
   {

      private String username;

      public CreateResetTokenTask(String username)
      {
         this.username = username;
      }

      @Override
      public TokenData<AccountRecord> execute(Connection conn) throws Exception
      {
         AccountRecord rv = loadAccount(conn);

         TokenData<AccountRecord> token = accountTokens.createTokenData(rv);
         setResetHash(username, token.getToken(), conn);

         return token;
      }

      private void setResetHash(String username, String token, Connection conn) throws SQLException, AccountException
      {
         String sql = format(SET_RESET_HASH, SQL_TABLENAME);
         try (PreparedStatement ps = conn.prepareStatement(sql))
         {
            ps.setString(1, token);
            ps.setString(2, username);

            int rows = ps.executeUpdate();
            if (rows != 1)
               throw new AccountException("Failed to reset password for user");
         }
      }

      private AccountRecord loadAccount(Connection conn) throws SQLException, AccountNotFoundException
      {
         String sql = format(GET_ACCOUNT_TEMPLATE, SQL_TABLENAME, SQL_COL_USERNAME);
         try (PreparedStatement ps = conn.prepareStatement(sql))
         {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
               throw new AccountNotFoundException(format("No user exists with name '{0}'", username));

            AccountRecord rv = new AccountRecord();
            rv.uid = rs.getLong("user_id");
            rv.username = username;
            rv.first = rs.getString("first_name");
            rv.last = rs.getString("last_name");
            rv.email = rs.getString("email");

            if (rv.email == null || rv.email.trim().isEmpty())
            {
               // maybe the client has some other way to get the token to the user.
               logger.warning(format("Ressting password for account [{0}]. No email address has been associate with this account.", username));
            }

            return rv;
         }
      }
   }


//
//               String token = tokenService.createToken(account.id, PasswordResetKeyProperty, 1, ChronoUnit.DAYS);
//               accountManager.modifyAccount(account.id, a -> a.resetToken = token);
//               String url = properties.getPropertyValue("edu.tamu.tcat.epss.mobile.server.accountPortal.baseurl", String.class) + "/index.html?resetToken=" + token + "#reset";
//
//               URL resetMessageUrl = Activator.getBundleContext().getBundle().getResource(ResetRequestFilePath);
//               String mailMessage;
//               try (InputStream input = resetMessageUrl.openStream())
//               {
//                  mailMessage = IOUtils.toString(input);
//               }
//               catch (IOException e)
//               {
//                  throw new EPSSException("Could not read mail template", e);
//               }
//               mailMessage = mailMessage.replace("${name}", accountName);
//               mailMessage = mailMessage.replace("${requestlink}", url);
//               mailMessage = mailMessage.replace("${password-reset-expire}", "1");
//
//               String mailHost = properties.getPropertyValue(PasswordResetHost, String.class);
//               String mailFrom = properties.getPropertyValue(PasswordResetFrom, String.class);
//               String subject = properties.getPropertyValue(PasswordResetSubject, String.class);
//               SendUserEmailCommand cmd = new SendUserEmailCommand(email, mailHost, mailFrom);
//               cmd.setMessageBody(mailMessage);
//               cmd.setMessageSubject(subject);
//               if (!cmd.send().isEmpty())
//                  throw new InternalServerErrorException();
//            }
//
//      }
//   }

   private class CreateAccountRecordTask implements SqlExecutor.ExecutorTask<AccountRecord>
   {
      private final String hashedPassword;
      private final AccountRecord data;

      private CreateAccountRecordTask(AccountRecord data, String hashedPassword)
      {
         this.hashedPassword = hashedPassword;
         this.data = data;
      }

      @Override
      public AccountRecord execute(Connection conn) throws Exception
      {
         String sql = format(CREATE_ACCOUNT_SQL, SQL_TABLENAME);
         try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
         {
            ps.setString(1, data.username);
            ps.setString(2, hashedPassword);
            ps.setString(3, data.first);
            ps.setString(4, data.last);
            ps.setString(5, data.email);

            ps.execute();
            try (ResultSet rs = ps.getGeneratedKeys())
            {
               if (!rs.next())
                  throw new IllegalStateException("Failed account creation");

               AccountRecord rv = new AccountRecord();
               rv.uid = rs.getLong("user_id");
               rv.username = data.username;
               rv.first = data.first;
               rv.last = data.last;
               rv.email = data.email;

               return rv;
            }
         }
      }
   }

   private final class GetAccountRecordTask implements SqlExecutor.ExecutorTask<AccountRecord>
   {
      private final String username;
      private final String rawPassword;

      private GetAccountRecordTask(String username, String rawPassword)
      {
         this.rawPassword = rawPassword;
         this.username = username;
      }

      @Override
      public AccountRecord execute(Connection conn) throws Exception
      {
         return getRecordFromDb(conn, username, rawPassword);
      }
   }

   private final class GetRecordByIdTask implements SqlExecutor.ExecutorTask<AccountRecord>
   {
      private final long uid;

      private GetRecordByIdTask(long uid)
      {
         this.uid = uid;
      }

      @Override
      public AccountRecord execute(Connection conn) throws Exception
      {
         String sql = format(GET_ACCOUNT_TEMPLATE,  SQL_TABLENAME, DATA_KEY_UID);
         try (PreparedStatement ps = conn.prepareStatement(sql))
         {
            ps.setLong(1, uid);
            try (ResultSet rs = ps.executeQuery())
            {
               if (!rs.next())
                  throw new AccountNotFoundException(format("No user exists with id [{0}]", uid));

               AccountRecord rv = new AccountRecord();
               rv.uid = rs.getLong("user_id");
               rv.username = rs.getString("user_name");
               rv.first = rs.getString("first_name");
               rv.last = rs.getString("last_name");
               rv.email = rs.getString("email");

               return rv;
            }
         }
      }
   }
}
