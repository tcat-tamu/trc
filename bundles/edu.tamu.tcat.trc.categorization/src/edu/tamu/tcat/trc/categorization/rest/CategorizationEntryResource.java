package edu.tamu.tcat.trc.categorization.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *  Represents a single entry within a categorization.
 *
 */
public class CategorizationEntryResource
{
   // TODO should be specific to categorization type?
   private String categorizationKey;
   private String entryKey;

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
   public RestApiV1.Categorization associateArticle(RestApiV1.ArticleReference article)
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
