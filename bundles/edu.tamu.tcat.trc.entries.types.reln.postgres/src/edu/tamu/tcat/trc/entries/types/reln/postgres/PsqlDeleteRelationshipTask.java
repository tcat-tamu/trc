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
package edu.tamu.tcat.trc.entries.types.reln.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;

public class PsqlDeleteRelationshipTask implements SqlExecutor.ExecutorTask<Void>
{
   private final static String insert = "UPDATE relationships"
                                      + "  SET active = ?,"
                                      + "      modified = now()"
                                      + "  WHERE id = ?";

   private final String id;

   public PsqlDeleteRelationshipTask(String id)
   {
      this.id = id;
   }

   @Override
   public Void execute(Connection conn) throws Exception
   {
      try(PreparedStatement ps = conn.prepareStatement(insert))
      {
         ps.setBoolean(1, false);
         ps.setString(2, id);

         int ct = ps.executeUpdate();
         if (ct != 1)
            throw new IllegalStateException("Failed to de-activate the relationship. Unexpected number of rows updated [" + ct + "]");

      }
      catch(SQLException e)
      {
         throw new IllegalStateException("Failed to create relationship: [" + id + "]");
      }
      return null;
   }
}
