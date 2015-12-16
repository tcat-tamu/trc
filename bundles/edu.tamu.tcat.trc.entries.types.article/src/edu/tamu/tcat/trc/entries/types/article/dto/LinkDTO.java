package edu.tamu.tcat.trc.entries.types.article.dto;

import edu.tamu.tcat.trc.entries.types.article.ArticleLink;

public class LinkDTO
{
   public String id;
   public String title;
   public String type;
   public String uri;
   public String rel;
   
   public static LinkDTO create(ArticleLink l)
   {
      LinkDTO dto = new LinkDTO();
      dto.id = l.getId();
      dto.title = l.getTitle();
      dto.type = l.getType();
      dto.uri = l.getUri();
      dto.rel = l.getRel();
      return dto;
   }
}
