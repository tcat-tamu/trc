package edu.tamu.tcat.trc.entries.reln.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.catalog.psql.ExecutionFailedException;

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
            throw new ExecutionFailedException("Failed to de-activate the relationship. Unexpected number of rows updated [" + ct + "]");

      }
      catch(SQLException e)
      {
         throw new IllegalStateException("Failed to create relationship: [" + id + "]");
      }
      return null;
   }
}
