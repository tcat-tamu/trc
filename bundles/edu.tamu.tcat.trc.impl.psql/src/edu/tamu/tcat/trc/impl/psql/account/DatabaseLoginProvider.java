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

import edu.tamu.tcat.account.db.login.AccountRecord;
import edu.tamu.tcat.account.db.login.DbLoginData;
import edu.tamu.tcat.account.login.LoginData;
import edu.tamu.tcat.account.login.LoginProvider;

/**
 * An implementation of a {@link LoginProvider} which performs authentication against a database.
 * @deprecated Should use e.t.t.account.db.login.DatabaseLoginProvider, but the auth manager is buggy
 */
@Deprecated
public class DatabaseLoginProvider implements LoginProvider
{
   private String userName;
   private String pass;
   private String instanceId;
   private DatabaseAuthnManager authnMananger;

   public void init(String providerId, String username, String password, DatabaseAuthnManager authnMananger)
   {
      this.instanceId = providerId;
      this.userName = username;
      this.pass = password;
      this.authnMananger = authnMananger;
   }

   @Override
   public LoginData login()
   {
      AccountRecord rec = authnMananger.authenticate(userName, pass);
      if (rec == null)
         return null;
      LoginData rv = new DbLoginData(instanceId, rec);
      return rv;
   }
}
