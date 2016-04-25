package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.copies.CopyReferenceDTO;

public class WorkChangeSet
{
   public String id;
   @Deprecated
   public String type;
   public List<AuthorReferenceDTO> authors = new ArrayList<>();
   public Collection<TitleDTO> titles = new ArrayList<>();
   public List<AuthorReferenceDTO> otherAuthors = new ArrayList<>();
   public List<EditionDTO> editions = new ArrayList<>();
   public String series;
   public String summary;
   public String defaultCopyReferenceId;
   public Set<CopyReferenceDTO> newCopyReferences = new HashSet<>();
   public Set<CopyReferenceDTO> removedCopyReferences = new HashSet<>();

   public WorkDTO original;

   public WorkChangeSet(String id)
   {
      this.id = id;
   }
}
