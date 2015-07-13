package edu.tamu.tcat.trc.entries.types.article.search.solr;

import java.util.Arrays;
import java.util.Collection;

import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchProxy;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.search.solr.impl.BasicFields;


public class ArticleSolrConfig implements SolrIndexConfig
{
   public static final SolrIndexField<String> ID = new BasicFields.BasicString("id");
   public static final SolrIndexField<String> TITLE = new BasicFields.BasicString("title");
   public static final SolrIndexField<String> AUTHOR_ID = new BasicFields.BasicString("author_id");
   public static final SolrIndexField<String> ASSOCIATED_ENTRY = new BasicFields.BasicString("associated_entry");
   public static final SolrIndexField<String> ARTICLE_CONTENT = new BasicFields.BasicString("article_content");
   public static final SolrIndexField<String> ARTICLE_MIME_TYPE = new BasicFields.BasicString("mime_type");

   public static final BasicFields.SearchProxyField<ArticleSearchProxy> SEARCH_PROXY =new BasicFields.SearchProxyField<ArticleSearchProxy>("article_dto", ArticleSearchProxy.class);

   @Override
   public Class<ArticleSearchProxy> getSearchProxyType()
   {
      return ArticleSearchProxy.class;
   }

   @Override
   public Class<ArticleDocument> getIndexDocumentType()
   {
      return ArticleDocument.class;
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getIndexedFields()
   {
      return Arrays.asList(ID, AUTHOR_ID, ASSOCIATED_ENTRY, ARTICLE_CONTENT, ARTICLE_MIME_TYPE);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getStoredFields()
   {
      return Arrays.asList(ID, AUTHOR_ID, ASSOCIATED_ENTRY, ARTICLE_CONTENT, ARTICLE_MIME_TYPE, SEARCH_PROXY);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getMultiValuedFields()
   {
      return Arrays.asList();
   }

}
