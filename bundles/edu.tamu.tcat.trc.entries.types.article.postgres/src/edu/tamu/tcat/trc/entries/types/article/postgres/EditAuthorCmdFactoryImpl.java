package edu.tamu.tcat.trc.entries.types.article.postgres;

import java.util.concurrent.Future;
import java.util.function.Supplier;

import edu.tamu.tcat.trc.entries.types.article.dto.ArticleAuthorDTO;
import edu.tamu.tcat.trc.entries.types.article.repo.EditAuthorCommand;
import edu.tamu.tcat.trc.repo.CommitHook;
import edu.tamu.tcat.trc.repo.EditCommandFactory;

public class EditAuthorCmdFactoryImpl implements EditCommandFactory<ArticleAuthorDTO, EditAuthorCommand>
{
   public EditAuthorCmdFactoryImpl()
   {
   }

   @Override
   public EditAuthorCmd create(String id, CommitHook<ArticleAuthorDTO> commitHook)
   {

      return new EditAuthorCmd(id, null, commitHook);
   }

   @Override
   public EditAuthorCmd edit(String id, Supplier<ArticleAuthorDTO> currentState, CommitHook<ArticleAuthorDTO> commitHook)
   {
      return new EditAuthorCmd(id, currentState, commitHook);
   }

   public class EditAuthorCmd implements EditAuthorCommand
   {
      private final Supplier<ArticleAuthorDTO> currentState;
      private final CommitHook<ArticleAuthorDTO> hook;
      private final AuthorChangeSet changes;

      EditAuthorCmd(String id, Supplier<ArticleAuthorDTO> currentState, CommitHook<ArticleAuthorDTO> hook)
      {
         this.currentState = currentState;
         this.hook = hook;
         this.changes = new AuthorChangeSet(id);
      }

      @Override
      public void setName(String name)
      {
         this.changes.name = name;
      }

      @Override
      public void setAffiliation(String affiliation)
      {
         this.changes.affiliation = affiliation;
      }

      @Override
      public void setEmail(String email)
      {
         this.changes.email = email;
      }

      @Override
      public Future<String> execute()
      {
         changes.original = (this.currentState != null) ? this.currentState.get() : null;
         ArticleAuthorDTO data = constructUpdatedData(changes.original);
         return hook.submit(data, changes);
      }

      private ArticleAuthorDTO constructUpdatedData(ArticleAuthorDTO original)
      {
         ArticleAuthorDTO data = new ArticleAuthorDTO();
         data.id = changes.id;
         data.name = changes.name;
         data.affiliation = changes.affiliation;
         data.email = changes.email;
         return data;
      }
   }
}
