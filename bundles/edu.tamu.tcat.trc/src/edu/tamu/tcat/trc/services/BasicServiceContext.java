package edu.tamu.tcat.trc.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.tamu.tcat.account.Account;

public class BasicServiceContext<T> implements ServiceContext<T>
{
   private final Class<T> type;
   private final Optional<Account> account;
   private final Map<String, Object> props;

   public BasicServiceContext(Class<T> type, Account account)
   {
      this(type, account, new HashMap<>());
   }

   public BasicServiceContext(Class<T> type, Account account, Map<String, Object> props)
   {
      this.type = type;
      this.account = account != null ? Optional.of(account) : Optional.empty();
      this.props = new HashMap<>(props);
   }

   @Override
   public Class<T> getType()
   {
      return type;
   }

   @Override
   public Optional<Account> getAccount()
   {
      return account;
   }

   @Override
   public Set<String> getScopeProperties()
   {
      return props.keySet();
   }

   @Override
   public Object getProperty(String key)
   {
      return props.get(key);
   }
}