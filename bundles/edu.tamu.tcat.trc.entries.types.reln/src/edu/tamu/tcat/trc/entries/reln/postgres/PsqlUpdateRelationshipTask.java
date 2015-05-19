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

public class PsqlUpdateRelationshipTask implements SqlExecutor.ExecutorTask<String>
{
   private final static String insert = "UPDATE relationships"
                                      + "  SET relationship = ?,"
                                      + "      modified = now()"
                                      + "  WHERE id = ?";

   private final RelationshipDV relationship;
   private final ObjectMapper jsonMapper;

   public PsqlUpdateRelationshipTask(RelationshipDV relationship, ObjectMapper jsonMapper)
   {
      this.relationship = relationship;
      this.jsonMapper = jsonMapper;
   }

   private String getJson()
   {
      try
      {
         return jsonMapper.writeValueAsString(relationship);
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

         ps.setObject(1, jsonObject);
         ps.setString(2, relationship.id);

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
