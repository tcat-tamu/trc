package edu.tamu.tcat.trc.entries.types.article.postgres;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.dto.ArticleAuthorDTO;
import edu.tamu.tcat.trc.entries.types.article.repo.EditAuthorCommand;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.RepositorySchema;
import edu.tamu.tcat.trc.repo.SchemaBuilder;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;

public class ArticleAuthorManager implements ArticleAuthor
{
   
   private SqlExecutor exec;
   private ConfigurationProperties config;
   
   DocumentRepository<ArticleAuthor, EditAuthorCommand> docRepos;
   
   public void setSqlExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }
   
   public void setConfig(ConfigurationProperties config)
   {
      this.config = config;
   }
   
   public void activate()
   {
      buildAuthorRepo();
   }
   
   private DocumentRepository<ArticleAuthor, EditAuthorCommand> buildAuthorRepo()
   {
      try
      {
         PsqlJacksonRepoBuilder<ArticleAuthor, EditAuthorCommand, ArticleAuthorDTO> repo = new PsqlJacksonRepoBuilder<>();
         repo.setDbExecutor(exec);
         repo.setTableName("authors");
   //      repo.setEditCommandFactory(EditCommandFactory<ArticleAuthor, ArticleAuthor, AotherEditCommand> cmdFacotry);
   //      repo.setDataAdapter(dto -> dto);
         repo.setSchema(buildSchema());
         repo.setStorageType(ArticleAuthorDTO.class);
         repo.setEnableCreation(true);
         return repo.build();
      }
      catch (RepositoryException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return null;
   }

   private RepositorySchema buildSchema()
   {
      SchemaBuilder schemaBuilder = new BasicSchemaBuilder();
      schemaBuilder.setId("trc.article.author");
      schemaBuilder.setDataField("author");
      return schemaBuilder.build();
   }

   @Override
   public String getId()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getName()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getAffiliation()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getEmail()
   {
      // TODO Auto-generated method stub
      return null;
   }


}
