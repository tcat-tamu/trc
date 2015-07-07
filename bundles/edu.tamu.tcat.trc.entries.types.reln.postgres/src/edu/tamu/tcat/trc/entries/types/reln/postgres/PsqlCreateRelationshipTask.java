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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.repo.ExecutionFailedException;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;

public class PsqlCreateRelationshipTask implements SqlExecutor.ExecutorTask<String>
{
   private final static String insert = "INSERT INTO relationships (id, relationship) VALUES(?,?)";

   private final RelationshipDTO relationship;
   private final ObjectMapper mapper;

   public PsqlCreateRelationshipTask(RelationshipDTO relationship, ObjectMapper jsonMapper)
   {
      this.relationship = relationship;
      this.mapper = jsonMapper;
   }

   private String getJson()
   {
      try
      {
         return mapper.writeValueAsString(relationship);
      }
      catch (IOException jpe)
      {
         throw new IllegalArgumentException("Failed to serialize the supplied relationship [" + relationship + "]", jpe);
      }
   }

   @Override
   public String execute(Connection conn) throws Exception
   {
      try(PreparedStatement ps = conn.prepareStatement(insert))
      {
         PGobject jsonObject = new PGobject();
         jsonObject.setType("json");
         jsonObject.setValue(getJson());

         ps.setString(1, relationship.id);
         ps.setObject(2, jsonObject);

         int ct = ps.executeUpdate();
         if (ct != 1)
            throw new ExecutionFailedException("Failed to create work. Unexpected number of rows updates [" + ct + "]");

      }
      catch(SQLException e)
      {
         throw new IllegalStateException("Failed to create relationship: [" + relationship + "]");
      }
      return relationship.id;
   }

}
