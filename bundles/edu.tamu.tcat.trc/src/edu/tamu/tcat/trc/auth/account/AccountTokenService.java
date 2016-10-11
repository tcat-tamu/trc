package edu.tamu.tcat.trc.auth.account;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.account.token.TokenService;
import edu.tamu.tcat.crypto.CryptoProvider;
import edu.tamu.tcat.crypto.SecureToken;
import edu.tamu.tcat.crypto.TokenException;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;

/**
 * The {@link TokenService} provided to the {@code TokenDynamicFeature} for the
 * use of {@code TokenSecured} and {@code TokenProviding} REST resource annotations.
 */
public class AccountTokenService implements TokenService<TrcAccount>
{
   private static final Logger debug = Logger.getLogger(AccountTokenService.class.getName());

   private static final String PROP_TOKEN_PROPERTY_KEY = "tokenKey";
   private static final String PROP_TOKEN_EXPIRES_KEY = "tokenKeyExpires";
   private static final String PROP_TOKEN_EXPIRES_UNIT_KEY = "tokenKeyExpiresUnit";

   private TrcAccountDataStore svcAcctStore;
   private ConfigurationProperties svcProps;
   private CryptoProvider svcCcrypto;

   private SecureToken secureToken;
   private long expire;
   private ChronoUnit expireUnit;

   void bind(TrcAccountDataStore svc)
   {
      this.svcAcctStore = svc;
   }

   void bind(ConfigurationProperties properties)
   {
      this.svcProps = properties;
   }

   void bind(CryptoProvider cryptoProvider)
   {
      this.svcCcrypto = cryptoProvider;
   }

   void activate(Map<String, Object> properties) throws Exception
   {
      try
      {
         String keyToken = Objects.requireNonNull((String)properties.get(PROP_TOKEN_PROPERTY_KEY), "[" + PROP_TOKEN_PROPERTY_KEY + "] must be set");
         String keyExpires = Objects.requireNonNull((String)properties.get(PROP_TOKEN_EXPIRES_KEY), "[" + PROP_TOKEN_EXPIRES_KEY + "] must be set");
         String keyExpiresUnit = Objects.requireNonNull((String)properties.get(PROP_TOKEN_EXPIRES_UNIT_KEY), "[" + PROP_TOKEN_EXPIRES_UNIT_KEY + "] must be set");

         String keyString = svcProps.getPropertyValue(keyToken, String.class);
         if (keyString == null)
         {
            debug.info("No token encryption key provided for '"+keyToken+"', AccountTokenService will not be available");
            return;
         }

         String expiresString = svcProps.getPropertyValue(keyExpires, String.class);
         if (expiresString == null)
         {
            debug.info("No value provided for '"+keyExpires+"', using default of '1'");
            expiresString = "1";
         }
         String expiresUnitString = svcProps.getPropertyValue(keyExpiresUnit, String.class);
         if (expiresUnitString == null)
         {
            debug.info("No token key provided for '"+keyExpiresUnit+"', using default of 'WEEKS'");
            expiresUnitString = ChronoUnit.WEEKS.name();
         }

         expire = Long.parseLong(expiresString);
         expireUnit = Objects.requireNonNull(ChronoUnit.valueOf(expiresUnitString));

         secureToken = decodeToken(keyString, keyToken);
      }
      catch (Exception e)
      {
         debug.log(Level.SEVERE, "Failed initializing", e);
      }
   }

   private SecureToken decodeToken(String keyString, String propertyKey)
   {
      byte[] key;
      try
      {
         key = Base64.getDecoder().decode(keyString);
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Could not decode token key in property [" + propertyKey + "]", e);
      }
      try
      {
         return svcCcrypto.getSecureToken(key);
      }
      catch (TokenException e)
      {
         throw new IllegalStateException("Could not create secure token for key in property [" + propertyKey + "]", e);
      }
   }

   @Override
   public TokenData<TrcAccount> createTokenData(TrcAccount payload)
   {
      if (secureToken == null)
         throw new IllegalStateException("Token service not initialized");
      ZonedDateTime now = ZonedDateTime.now();
      ZonedDateTime expireDateTime = now.plus(expire, expireUnit);
      String tokenStr = createToken(getId(payload), expireDateTime);

      return new TokenData<TrcAccount>()
      {
         @Override
         public String getToken()
         {
            return tokenStr;
         }

         @Override
         public TrcAccount getPayload()
         {
            return payload;
         }

         @Override
         public ZonedDateTime getExpiration()
         {
            return expireDateTime;
         }
      };
   }

   protected UUID getId(TrcAccount account)
   {
      return account.getId();
   }

   protected TrcAccount getPayload(UUID id)
   {
      return svcAcctStore.getAccount(id);
   }

   @Override
   public Class<TrcAccount> getPayloadType()
   {
      return TrcAccount.class;
   }

   @Override
   public TrcAccount unpackToken(String token)
   {
      if (secureToken == null)
         throw new IllegalStateException("Token service not initialized");
      try
      {
         ByteBuffer content = secureToken.getContentFromToken(token);
         if (content.limit() != 4 + 8 + 16)
            throw new IllegalArgumentException("Invalid token");
         int version = content.getInt();
         if (version != 1)
            throw new IllegalArgumentException("Invalid token");
         Instant expiry = Instant.ofEpochMilli(content.getLong());
         if (expiry.isBefore(Instant.now()))
            throw new IllegalArgumentException("Invalid token");
         long mostSigBits = content.getLong();
         long leastSigBits = content.getLong();
         UUID id = new UUID(mostSigBits, leastSigBits);
         return Objects.requireNonNull(getPayload(id));
      }
      catch (TokenException e)
      {
         throw new IllegalArgumentException("Could not read token", e);
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException("Could not get account", e);
      }
   }

   private String createToken(UUID id, ZonedDateTime expires)
   {
      ByteBuffer buffer = ByteBuffer.allocate(4 + 8 + 16);
      buffer.putInt(1);
      buffer.putLong(Instant.from(expires).toEpochMilli());
      buffer.putLong(id.getMostSignificantBits());
      buffer.putLong(id.getLeastSignificantBits());
      buffer.flip();
      try
      {
         return secureToken.getToken(buffer);
      }
      catch (TokenException e)
      {
         throw new IllegalArgumentException("Could not create token", e);
      }
   }
}
