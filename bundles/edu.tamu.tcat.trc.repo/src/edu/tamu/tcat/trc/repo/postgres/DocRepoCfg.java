package edu.tamu.tcat.trc.repo.postgres;

import java.util.function.Function;

import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.RepositorySchema;

public class DocRepoCfg<RecordType, DTO, EditCommandType>
{
   public String tablename;
   public RepositorySchema schema;
   public Function<DTO, RecordType> adapter;
   public Class<DTO> storageType;

   public EditCommandFactory<DTO, EditCommandType> cmdFactory;

   public String getRecordSql;
   public String createRecordSql;
   public String updateRecordSql;
   public String removeRecordSql;

   public DocRepoCfg()
   {
      // TODO Auto-generated constructor stub
   }

}
