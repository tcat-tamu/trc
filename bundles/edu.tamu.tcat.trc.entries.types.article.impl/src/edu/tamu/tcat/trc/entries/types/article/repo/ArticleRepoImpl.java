package edu.tamu.tcat.trc.entries.types.article.repo;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.repo.DocumentRepository;

public class ArticleRepoImpl implements ArticleRepository
{
   private DocumentRepository<Article, EditArticleCommand> repoBackend;

   @Override
   public Article get(UUID articleId) throws NoSuchCatalogRecordException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<Article> getArticles(URI entityURI) throws NoSuchCatalogRecordException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public EditArticleCommand create()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public EditArticleCommand edit(UUID articleId) throws NoSuchCatalogRecordException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Future<Boolean> remove(UUID articleId)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public AutoCloseable register(UpdateListener<ArticleChangeEvent> ears)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
