package edu.tamu.tcat.trc.entries.types.article.rest.v1;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchProxy;

public class ArticleSearchAdapter
{
   public static List<RestApiV1.ArticleSearchResult> toDTO(List<ArticleSearchProxy> origList)
   {
      List<RestApiV1.ArticleSearchResult> dtoList = new ArrayList<>();
      if (origList == null)
         return dtoList;
      
      origList.forEach((article) ->
      {
         RestApiV1.ArticleSearchResult dto = new RestApiV1.ArticleSearchResult();
         dto.id = article.id;
         dto.authorId = article.authorId;
         dto.associatedEntity = article.associatedEntity;
         dto.mimeType = article.mimeType;
         dto.title = article.title;
         dto.content = article.content;
         
         dtoList.add(dto);
      });
      
      return dtoList;
   }
}
