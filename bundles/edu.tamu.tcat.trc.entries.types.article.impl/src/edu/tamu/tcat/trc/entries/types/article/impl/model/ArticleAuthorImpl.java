package edu.tamu.tcat.trc.entries.types.article.impl.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.DataModelV1;

public class ArticleAuthorImpl implements ArticleAuthor
{
   private final String id;
   private final String name;
   private final String firstname;
   private final String lastname;
   private final Map<String, String> properties = new HashMap<>();

   public ArticleAuthorImpl(DataModelV1.ArticleAuthor dto)
   {
      id = dto.id;
      name = dto.name;
      firstname = dto.first;
      lastname = dto.last;

      if (dto.properties != null)
         properties.putAll(dto.properties);
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String getDisplayName()
   {
      return name;
   }

   @Override
   public String getFirstname()
   {
      return firstname;
   }

   @Override
   public String getLastname()
   {
      return lastname;
   }

   @Override
   public Set<String> getProperties()
   {
      return properties.keySet();
   }

   @Override
   public Optional<String> getProperty(String key)
   {
      // TODO: Key could be missing or mapped to null value, hence we use Optional.ofNullable here.
      //       This returns an empty Optional in either case.
      return Optional.ofNullable(properties.get(key));
   }

}
