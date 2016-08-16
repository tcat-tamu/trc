package edu.tamu.tcat.trc.categorization.rest;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.categorization.CategorizationRepo;
import edu.tamu.tcat.trc.categorization.CategorizationRepoFactory;
import edu.tamu.tcat.trc.categorization.CategorizationScope;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;

/**
 * Point of entry to the REST API for the Categorization system. This class is
 * designed to be configured and registered as an OSGi declarative service.
 *
 */
@Path("/")
public class CategorizationAPIService
{

   private CategorizationRepoFactory repoProvider;
   private EntryResolverRegistry resolvers;
   private ConfigurationProperties config;

   public void bind(CategorizationRepoFactory repoProvider)
   {
      this.repoProvider = repoProvider;
   }

   public void bind(EntryResolverRegistry resolvers)
   {
      this.resolvers = resolvers;
   }

   public void bind(ConfigurationProperties config)
   {
      // TODO alternatively, we could bind the core TRC Facade
      this.config = config;
   }

   public void activate()
   {

   }

   @Path("/categorizations/{scope}")
   public CategorizationSchemesResource get(@PathParam("scope") @DefaultValue("default") String scopeId)
   {
      // TODO may adapt scope by translating username into account id

      CategorizationScope scope = repoProvider.createScope(null, scopeId);
      CategorizationRepo repository = repoProvider.getRepository(scope);

      return new CategorizationSchemesResource(repository);
   }
}
