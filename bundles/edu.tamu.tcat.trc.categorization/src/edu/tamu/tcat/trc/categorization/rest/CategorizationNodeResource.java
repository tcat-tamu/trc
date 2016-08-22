package edu.tamu.tcat.trc.categorization.rest;

import static java.text.MessageFormat.format;

import java.util.logging.Level;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.trc.categorization.CategorizationNode;
import edu.tamu.tcat.trc.categorization.CategorizationRepo;
import edu.tamu.tcat.trc.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.categorization.strategies.tree.TreeCategorization;
import edu.tamu.tcat.trc.categorization.strategies.tree.TreeNode;

/**
 *  Represents a single entry within a categorization.
 *
 */
public abstract class CategorizationNodeResource<SchemeType extends CategorizationScheme>
{
   protected final CategorizationRepo repo;
   protected final SchemeType scheme;
   protected final String nodeId;

   public CategorizationNodeResource(CategorizationRepo repo, SchemeType scheme, String nodeId)
   {
      this.repo = repo;
      this.scheme = scheme;
      this.nodeId = nodeId;
   }

   protected abstract CategorizationNode resolveNode();

   /**
    * @return An instance of this entry.
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Categorization getEntry()
   {
      return null;
   }

   public void remove() {

   }

   public void update() {

   }

   @PUT
   @Path("articleRef")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Categorization associateArticle(RestApiV1.EntryReference article)
   {
      return null;
   }

   @GET
   @Path("article")
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Categorization getArticle()
   {
      return null;
   }

   public static class TreeNodeResource extends CategorizationNodeResource<TreeCategorization>
   {
      public TreeNodeResource(CategorizationRepo repo, TreeCategorization scheme, String nodeId)
      {
         super(repo, scheme, nodeId);
      }

      @Override
      protected TreeNode resolveNode()
      {
         try
         {
            return scheme.getNode(nodeId);
         }
         catch (IllegalArgumentException iae)
         {
            throw ModelAdapterV1.raise(Response.Status.NOT_FOUND, format("Not Found"), Level.WARNING, iae);
         }
      }

      public void createChild()
      {

      }

      public void getParent()
      {

      }

      public void getChildren()
      {

      }
   }
}
