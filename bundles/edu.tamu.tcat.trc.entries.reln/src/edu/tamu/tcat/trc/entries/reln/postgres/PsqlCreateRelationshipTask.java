package edu.tamu.tcat.trc.entries.reln.postgres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.catalog.psql.ExecutionFailedException;
import edu.tamu.tcat.trc.entries.reln.model.RelationshipDV;

public class PsqlCreateRelationshipTask implements SqlExecutor.ExecutorTask<String>
{
   private final static String insert = "INSERT INTO relationships (id, relationship) VALUES(?,?)";

   private final RelationshipDV relationship;
   private final ObjectMapper mapper;

   public PsqlCreateRelationshipTask(RelationshipDV relationship, ObjectMapper jsonMapper)
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
