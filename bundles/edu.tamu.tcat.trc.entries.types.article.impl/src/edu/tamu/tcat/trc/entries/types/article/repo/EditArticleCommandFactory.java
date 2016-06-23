package edu.tamu.tcat.trc.entries.types.article.repo;

import java.util.function.Supplier;

import edu.tamu.tcat.trc.repo.CommitHook;
import edu.tamu.tcat.trc.repo.EditCommandFactory;

public class EditArticleCommandFactory implements EditCommandFactory<DataModelV1.Article, EditArticleCommand>
{

   @Override
   public EditArticleCommand create(String id, CommitHook<DataModelV1.Article> commitHook)
   {
      new EditArticleCommandImpl(id, exec)
   }

   @Override
   public EditArticleCommand edit(String id, Supplier<DataModelV1.Article> currentState, CommitHook<DataModelV1.Article> commitHook)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
