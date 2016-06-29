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
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.repo.NoSuchEntryException;

public class PsqlGetRelationshipTask implements SqlExecutor.ExecutorTask<Relationship>
{
   private final static String select = "SELECT relationship FROM relationships"
                                      + "  WHERE id=? AND active=true";

   private final ObjectMapper jsonMapper;
   private final String id;
   private final RelationshipTypeRegistry typeReg;

   public PsqlGetRelationshipTask(String id, ObjectMapper jsonMapper, RelationshipTypeRegistry typeReg)
   {
      this.id = id;
      this.jsonMapper = jsonMapper;
      this.typeReg = typeReg;
   }

   @Override
   public Relationship execute(Connection conn) throws Exception
   {
      try (PreparedStatement ps = conn.prepareStatement(select))
      {
         ps.setString(1, id);
         try (ResultSet rs = ps.executeQuery())
         {
            if (!rs.next())
               throw new NoSuchEntryException("No catalog record exists for work id=" + id);

            PGobject pgo = (PGobject)rs.getObject("relationship");
            String relationshipJson = pgo.toString();
            try
            {
               RelationshipDTO dv = jsonMapper.readValue(relationshipJson, RelationshipDTO.class);
               return RelationshipDTO.instantiate(dv, typeReg);
            }
            catch (IOException e)
            {
               throw new IllegalStateException("Failed to parse relationship record\n" + relationshipJson, e);
            }
         }
      }
      catch (SQLException e)
      {
         throw new IllegalStateException("Failed to retrieve relationship entry [entry id = " + id + "]", e);
      }
   }



}
