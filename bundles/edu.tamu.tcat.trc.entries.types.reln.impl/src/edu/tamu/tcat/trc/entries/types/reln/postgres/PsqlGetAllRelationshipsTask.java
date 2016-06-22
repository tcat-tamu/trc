package edu.tamu.tcat.trc.entries.types.reln.postgres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;

public class PsqlGetAllRelationshipsTask implements SqlExecutor.ExecutorTask<List<Relationship>>
{
   private final static String select = "SELECT relationship FROM relationships WHERE active=true";

   private final ObjectMapper jsonMapper;
   private final RelationshipTypeRegistry typeReg;

   public PsqlGetAllRelationshipsTask(ObjectMapper jsonMapper, RelationshipTypeRegistry typeReg)
   {
      this.jsonMapper = jsonMapper;
      this.typeReg = typeReg;
   }

   @Override
   public List<Relationship> execute(Connection conn) throws Exception
   {
      List<Relationship> results = new ArrayList<>();

      try (PreparedStatement ps = conn.prepareStatement(select)) {
         try (ResultSet rs = ps.executeQuery()) {

            while (rs.next())
            {
               PGobject pgo = (PGobject)rs.getObject("relationship");
               String relationshipJson = pgo.toString();
               try {
                  RelationshipDTO dv = jsonMapper.readValue(relationshipJson, RelationshipDTO.class);
                  Relationship relationship = RelationshipDTO.instantiate(dv, typeReg);
                  results.add(relationship);
               }
               catch (IOException e) {
                  throw new IllegalStateException("Failed to parse relationship record\n" + relationshipJson, e);
               }
            }
         }
      }
      catch (SQLException e) {
         throw new IllegalStateException("Failed to retrieve relationships", e);
      }

      return results;
   }
}
