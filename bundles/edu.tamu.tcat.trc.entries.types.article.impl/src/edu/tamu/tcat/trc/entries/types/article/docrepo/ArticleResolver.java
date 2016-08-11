package edu.tamu.tcat.trc.entries.types.article.docrepo;

import java.net.URI;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.EntryReference;
import edu.tamu.tcat.trc.entries.core.EntryResolver;
import edu.tamu.tcat.trc.entries.core.InvalidReferenceException;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;

public class ArticleResolver implements EntryResolver<Article>
{
   private final ArticleRepoService articleSvc;
   private URI apiEndpoint;

   public ArticleResolver(ArticleRepoService articleSvc, ConfigurationProperties config)
   {
      this.articleSvc = articleSvc;
      this.apiEndpoint = config.getPropertyValue("trc.api.endpoint", URI.class, URI.create("")); // FIXME magic string
   }

   @Override
   public Article resolve(Account account, EntryReference reference) throws InvalidReferenceException
   {
      if (!accepts(reference))
         throw new InvalidReferenceException(reference, "Unsupported reference type.");

      ArticleRepository repo = articleSvc.getArticleRepo(account);
      return repo.get(reference.id);
   }

   @Override
   public URI toUri(EntryReference reference) throws InvalidReferenceException
   {
      if (!accepts(reference.type))
         throw new InvalidReferenceException(reference, "Unsupported reference type.");

      // format: <api_endpoint>/entries/articles/{articleId}
      return apiEndpoint.resolve(ArticleRepository.ENTRY_URI_BASE).resolve(reference.id);
   }

   @Override
   public EntryReference makeReference(Article instance) throws InvalidReferenceException
   {
      EntryReference ref = new EntryReference();
      ref.id = instance.getId();
      ref.type = ArticleRepository.ENTRY_TYPE_ID;

      return ref;
   }

   @Override
   public EntryReference makeReference(URI uri) throws InvalidReferenceException
   {
      URI articleId = uri.relativize(apiEndpoint.resolve(ArticleRepository.ENTRY_URI_BASE));
      if (articleId.equals(uri))
         throw new InvalidReferenceException(uri, "The supplied URI does not reference an article.");

      String path = articleId.getPath();
      if (path.contains("/"))
         throw new InvalidReferenceException(uri, "The supplied URI represents a sub-resource of an article.");

      EntryReference ref = new EntryReference();
      ref.id = path;
      ref.type = ArticleRepository.ENTRY_TYPE_ID;

      return ref;
   }

   @Override
   public boolean accepts(Object obj)
   {
      return (ArticleImpl.class.isInstance(obj));
   }

   @Override
   public boolean accepts(EntryReference ref)
   {
      return ArticleRepository.ENTRY_TYPE_ID.equals(ref.type);
   }

   @Override
   public boolean accepts(URI uri)
   {
      URI articleId = uri.relativize(apiEndpoint.resolve(ArticleRepository.ENTRY_URI_BASE));
//         The supplied URI does not reference an article
      if (articleId.equals(uri))
         return false;

      String path = articleId.getPath();
//         The supplied URI represents a sub-resource of an article.
      if (path.contains("/"))
         return false;

      return true;
   }
}