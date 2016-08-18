package edu.tamu.tcat.trc.categorization.rest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class RestApiV1
{
   public static class Categorization
   {
      /**
       * Basic information about this categorization including it's id, version,
       * and dates of creation/modification.
       */
      public CategorizationMeta meta = new CategorizationMeta();

      public String key;

      /** A label for this Categorization for display. */
      public String label;

      /** A brief description outlining the purpose of this categorization. */
      public String description;

      /** The type of categorization. */
      public String type;

      /**
       * The entries associated with this {@code Categorization}. The type of
       * categorization will determine the type of this field. For instance,
       * a hierarchical categorization uses a {@link BasicEntry} while a
       * list or group uses a List<> or Set<> correspondingly.
       */
      public List<BasicEntry> entries;

      public HierarchyEntry root;
   }

   public static class CategorizationMeta
   {
      /**
       * The unique id for this categorization.
       */
      public String id;

      /**
       * The version of this categorization. The version number is monotonically
       * increasing and represents the last time this categorization was
       * synchronized with the server.
       */
      public long version;

      /** ISO 8601 timestamp for when this categorization was created. */
      public String dateCreated;

      /** ISO 8601 timestamp for when this categorization was last modified. */
      public String dateModified;
   }

   /**
    * Represents a hierarchical categorization scheme.
    */
   public static class BasicEntry
   {
      // TODO add HATEOS links

      /** A persistent, globally unique identifier for this entry. */
      public String id;

      /** The Id of the categorization this entry belongs to. */
      public String schemeId;

      /** The version of the categorization when this entry was last modified. */
      public long version;

      /** A short human-readable identifier for this entry. Most be
       *  unique within a {@link Categorization}. Optional. */
      public String slug;

      /** A label for this node suitable for display as the primary identifier of this
       *  entry. Must not be {@code null}. */
      public String label;

      /** A description of this entry. */
      public String description;

      /** A reference to the associated article entry or null if no article has been
       *  associated with this entry. */
      public EntryReference entryRef;
   }

   /**
    * Represents a hierarchical categorization scheme.
    */
   public static class HierarchyEntry extends BasicEntry
   {
      /** The ids of the children of this entry. Note that this is the authoritative
       *  record  child entries associated with this entry, while {@link #children} is
       *  used for eager fetching of data. */
      public List<String> childIds;

      /** The children of this entry. For entries that have not been fully, restored,
       *  this may be {@code null}. In this case, the entries should be loaded by
       *  hitting the node's API endpoint. */
      public List<BasicEntry> children;
   }

   /**
    *  In addition to core categorization entry fields, provides additional detail about the
    *  associated article (if any).
    */
   public static class ArticleAbstractEntry extends BasicEntry
   {
      /** The abstract of the associated article. May be {@code null} or empty string if no
       *  article has been associated with the entry. */
      @JsonProperty("abstract")
      public String articleAbstract;

      /** The authors associated with this entry.  */
      public List<String> authors;
   }

   /** A simple data structure that reference a TRC Entry. */
   public static class EntryReference
   {
      /** The semantic type of the referenced entry. */
      public String type;

      /** The id of the referenced entry. */
      public String id;

      /**
       * The version of the referenced article. If zero (or negative), this will
       * reference the latest version.
       */
      public int version = 0;
   }

   public static enum ChangeDisposition
   {
      pending, completed, rejected;
   }

   public static class ChangeMeta
   {
      /** The specific type of change. */
      public String type;

      /** A unique identifier for this change action. Should not be supplied for updates. */
      public String changeId;

      /** The unique id of the categorization to which this change was applied. */
      public String categorizationId;

      /** The version id in which this change took effect. */
      public long versionId;

      /** The timestamp when this change took effect in ISO-8601 ZonedDateTime format. */
      public String timestamp;

      /** The account id of the user who submitted this change. */
      public String account;

      /** The current status of this change. */
      public ChangeDisposition disposition;

      /** Any user supplied comments regarding the pupose or intent of this change. */
      public String comments;
   }

   /**
    * The details of a change applied to a specific entry.
    */
   public static class EntryChangeAction
   {
      /** Information about this change. */
      public ChangeMeta meta;

      /** The details of of the specific change described by this action. The
       *  concrete type of this property is specific to different types of changes. */
      public ChangeDetails action;
   }

   public static class ChangeDetails
   {
      public String entryId;
   }

   /**
    *  Represents the movement of an entry within a list, hierarchy or graph
    *  structure.
    */
   public static class MoveEntry extends ChangeDetails
   {
      public String type = "Move Entry";

      /** The entry being moved. */
      public String entryId;

      /** The id of the new parent for this entry. If not supplied for hierarchical
       *  entry types, this will assume a move within the list of the entry's current
       *  parent. This property will be ignored for List categorizations. . */
      public String parentId;

      /** The zero-based index of this entry among its new siblings.
       *  Optional. If not supplied the entry will be added to the end
       *  of the list. If zero or negative the entry will be added as
       *  to the beginning of the list. */
      public long position;

      /** The id of the original parent of this entry. Should not be supplied on
       *  update requests. Will not be */
      public String origParent;

      /** The original index of this entry among its siblings. Should not be
       *  supplied on update requests. */
      public long origPosition;
   }

   // TODO flesh out these actions

   public static class CreateEntry extends ChangeDetails
   {

   }

   public static class DeleteEntry extends ChangeDetails
   {

   }

   public static class UpdateEntry extends ChangeDetails
   {

   }

   public static class AssociateArticle extends ChangeDetails
   {

   }
}
