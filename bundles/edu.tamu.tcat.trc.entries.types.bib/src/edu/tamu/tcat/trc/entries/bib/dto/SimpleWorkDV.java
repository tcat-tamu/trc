package edu.tamu.tcat.trc.entries.bib.dto;

import java.util.List;

public class SimpleWorkDV
{
   // FIXME this is internal to the search system. Does not belong here.

   public String id;
   public List<String> authorIds;
   public List<String> authorNames;
   public List<String> authorRole;
   public List<String> titleTypes;
   public List<String> lang;
   public List<String> titles;
   public List<String> subtitles;
   public String series;
   public String summary;

   public String _version_;

   public SimpleWorkDV()
   {

   }

   public SimpleWorkDV(WorkDV works)
   {
      this.id = works.id;
      for (AuthorRefDV author : works.authors)
      {
         authorIds.add(author.authorId);
         authorNames.add(author.name);
         authorRole.add(author.role);
      }

      for (TitleDV title : works.titles)
      {
         titles.add(title.title);
         subtitles.add(title.subtitle);
         lang.add(title.lg);
         titleTypes.add(title.type);
      }

      this.series = works.series;
      this.summary = works.summary;

      this._version_ = "";
   }
}
