package edu.tamu.tcat.trc.entries.types.article.postgres;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.AuthorManager;
import edu.tamu.tcat.trc.entries.types.article.dto.ArticleAuthorDTO;
import edu.tamu.tcat.trc.entries.types.article.repo.EditAuthorCommand;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.RepositorySchema;
import edu.tamu.tcat.trc.repo.SchemaBuilder;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;

public class ArticleAuthorManager implements AuthorManager
{

   private SqlExecutor exec;
   private ConfigurationProperties config;

   private DocumentRepository<ArticleAuthor, EditAuthorCommand> docRepos;
   
   public ArticleAuthorManager()
   {
   }

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
      this.docRepos = buildAuthorRepo();
   }

   private DocumentRepository<ArticleAuthor, EditAuthorCommand> buildAuthorRepo()
   {
      try
      {
         PsqlJacksonRepoBuilder<ArticleAuthor, EditAuthorCommand, ArticleAuthorDTO> repo = new PsqlJacksonRepoBuilder<>();
         repo.setDbExecutor(exec);
         repo.setTableName("authors");
         repo.setEditCommandFactory(new EditAuthorCmdFactoryImpl());
         repo.setDataAdapter(this::adapt);
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
      schemaBuilder.setId("trcArticleAuthor");
      schemaBuilder.setDataField("author");
      return schemaBuilder.build();
   }

   private ArticleAuthor adapt(ArticleAuthorDTO dto)
   {
      return new BasicAuthor(dto.id, dto.name, dto.affiliation, dto.email);
   }

   @Override
   public DocumentRepository<ArticleAuthor, EditAuthorCommand> getAuthorRepo()
   {
      return docRepos;
   }
}
