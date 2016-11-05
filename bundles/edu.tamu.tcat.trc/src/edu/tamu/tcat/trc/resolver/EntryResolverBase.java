package edu.tamu.tcat.trc.resolver;

import java.net.URI;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.ConfigParams;

/**
 *  {@link EntryResolver} base class designed to simplify the the implementation of
 *  resolvers that follow a common pattern.
 *
 *  @param <EntryType> The type of entry supported by this resolver.
 */
public abstract class EntryResolverBase<EntryType> implements EntryResolver<EntryType>
{
   protected final URI apiEndpoint;
   protected final String uriBase;
   protected final String typeId;
   private Class<EntryType> entryType;

   /**
    *
    * @param type a Java type token representing the class of this entry resolver. Note that
    *       unless a resolver is expected to handle multiple types of implementation classes,
    *       this should be the implementation class rather than the the interface that defines
    *       the API for this type of entry.
    * @param config
    * @param entryUriBase
    * @param entryTypeId
    */
   public EntryResolverBase(Class<EntryType> type,
                            ConfigurationProperties config,
                            String entryUriBase,
                            String entryTypeId)
   {
      this.entryType = type;
      this.apiEndpoint = config.getPropertyValue(ConfigParams.API_ENDPOINT_PARAM, URI.class, URI.create(""));

      this.uriBase = entryUriBase;
      this.typeId = entryTypeId;
   }

   protected abstract String getId(EntryType instance);

   @Override
   public Class<EntryType> getType()
   {
      return entryType;
   }

   @Override
   public URI toUri(EntryId reference) throws InvalidReferenceException
   {
      if (!accepts(reference))
         throw new InvalidReferenceException(reference, "Unsupported reference type.");

      // format: <api_endpoint>/entries/biographical/{articleId}
      return apiEndpoint.resolve(this.uriBase).resolve(reference.getId());
   }

   @Override
   public EntryId makeReference(EntryType instance) throws InvalidReferenceException
   {
      return new EntryId(getId(instance), this.typeId);
   }

   @Override
   public EntryId makeReference(URI uri) throws InvalidReferenceException
   {
      URI articleId = uri.relativize(apiEndpoint.resolve(this.uriBase));
      if (articleId.equals(uri))
         throw new InvalidReferenceException(uri, "The supplied URI does not reference an article.");

      String path = articleId.getPath();
      if (path.contains("/"))
         throw new InvalidReferenceException(uri, "The supplied URI represents a sub-resource of an article.");

      return new EntryId(path, typeId);
   }

   @Override
   public boolean accepts(Object obj)
   {
      // TODO this should only support instances created by this repo, however the
      //      public DTO bundle means that there is no guarantee where an instance came from.
      return (entryType.isInstance(obj));
   }

   @Override
   public boolean accepts(EntryId ref)
   {
      return this.typeId.equals(ref.getType());
   }

   @Override
   public boolean accepts(URI uri)
   {
      URI entryId = uri.relativize(apiEndpoint.resolve(this.uriBase));
      if (entryId.equals(uri))
         return false;

      String path = entryId.getPath();
      if (path.contains("/"))
         return false;

      return true;
   }

}
