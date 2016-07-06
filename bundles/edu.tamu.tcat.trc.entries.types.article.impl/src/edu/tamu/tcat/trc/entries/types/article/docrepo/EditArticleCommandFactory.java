package edu.tamu.tcat.trc.entries.types.article.docrepo;

import edu.tamu.tcat.trc.entries.types.article.docrepo.DataModelV1.Article;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.repo.EditCommandFactory;

public class EditArticleCommandFactory implements EditCommandFactory<DataModelV1.Article, EditArticleCommand>
{

   public EditArticleCommandFactory()
   {
      // TODO Auto-generated constructor stub
   }

   @Override
   public EditArticleCommand create(String id, UpdateStrategy<Article> strategy)
   {
      return new EditArticleCommandImpl(id, strategy);
   }

   @Override
   public EditArticleCommand edit(String id, UpdateStrategy<Article> strategy)
   {
      return new EditArticleCommandImpl(id, strategy);
   }

}
