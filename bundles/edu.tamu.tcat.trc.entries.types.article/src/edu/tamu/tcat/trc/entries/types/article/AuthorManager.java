package edu.tamu.tcat.trc.entries.types.article;

import edu.tamu.tcat.trc.entries.types.article.repo.EditAuthorCommand;
import edu.tamu.tcat.trc.repo.DocumentRepository;

public interface AuthorManager
{
   //Empty interface for OSGI services to start ArticleAuthorManager
   DocumentRepository<ArticleAuthor, EditAuthorCommand> getAuthorRepo();
}
