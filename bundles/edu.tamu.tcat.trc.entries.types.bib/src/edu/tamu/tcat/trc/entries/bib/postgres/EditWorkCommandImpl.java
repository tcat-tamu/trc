package edu.tamu.tcat.trc.entries.bib.postgres;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.tamu.tcat.catalogentries.IdFactory;
import edu.tamu.tcat.catalogentries.InvalidDataException;
import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.bib.EditWorkCommand;
import edu.tamu.tcat.trc.entries.bib.EditionMutator;
import edu.tamu.tcat.trc.entries.bib.dto.AuthorRefDV;
import edu.tamu.tcat.trc.entries.bib.dto.EditionDV;
import edu.tamu.tcat.trc.entries.bib.dto.TitleDV;
import edu.tamu.tcat.trc.entries.bib.dto.VolumeDV;
import edu.tamu.tcat.trc.entries.bib.dto.WorkDV;

public class EditWorkCommandImpl implements EditWorkCommand
{
//   private static final Logger logger = Logger.getLogger(EditWorkCommandImpl.class.getName());

   private final WorkDV work;
   private final IdFactory idFactory;

   private Function<WorkDV, Future<String>> commitHook;

   EditWorkCommandImpl(WorkDV work, IdFactory idFactory)
   {
      this.work = work;
      this.idFactory = idFactory;
   }

   public void setCommitHook(Function<WorkDV, Future<String>> hook)
   {
      commitHook = hook;
   }

   @Override
   public void setAll(WorkDV work) throws InvalidDataException
   {
      setSeries(work.series);
      setSummary(work.summary);
      setAuthors(work.authors);
      setOtherAuthors(work.otherAuthors);
      setTitles(work.titles);

      setEditions(work.editions);
   }

   private void setEditions(Collection<EditionDV> editions)
   {
      // get IDs supplied by the client; after the update, these IDs should be the only ones in the database
      Set<String> clientIds = editions.parallelStream()
            .map(e -> e.id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

      // remove any editions that were removed by the client
      work.editions.removeIf(e -> !clientIds.contains(e.id));

      // create or update client-supplied editions
      for (EditionDV edition : editions) {
         EditionMutator mutator;

         try {
            mutator = (null == edition.id) ? createEdition() : editEdition(edition.id);
         }
         catch (NoSuchCatalogRecordException e) {
            throw new InvalidDataException("Failed to edit existing edition. A supplied edition contains an id [" + edition.id + "], but the identified edition cannot be retrieved for editing.", e);
         }

         mutator.setAll(edition);
      }
   }

   @Override
   public void setSeries(String series)
   {
      work.series = series;
   }

   @Override
   public void setSummary(String summary)
   {
      work.summary = summary;
   }

   @Override
   public void setAuthors(List<AuthorRefDV> authors)
   {
      work.authors = new ArrayList<>(authors);
   }

   @Override
   public void setOtherAuthors(List<AuthorRefDV> authors)
   {
      work.otherAuthors = new ArrayList<>(authors);
   }

   @Override
   public void setTitles(Collection<TitleDV> titles)
   {
      work.titles = new HashSet<>(titles);
   }

//   @Override
//   public void setPublicationDate(Date pubDate)
//   {
//      if (null == work.pubInfo) {
//         work.pubInfo = new PublicationInfoDV();
//      }
//
//      if (null == work.pubInfo.date) {
//         work.pubInfo.date = new DateDescriptionDV();
//      }
//
//      work.pubInfo.date.value = pubDate;
//   }
//
//   @Override
//   public void setPublicationDateDisplay(String display)
//   {
//      if (null == work.pubInfo) {
//         work.pubInfo = new PublicationInfoDV();
//      }
//
//      if (null == work.pubInfo.date) {
//         work.pubInfo.date = new DateDescriptionDV();
//      }
//
//      work.pubInfo.date.display = display;
//   }

   @Override
   public EditionMutator createEdition()
   {
      EditionDV edition = new EditionDV();
      edition.id = idFactory.getNextId(PsqlWorkRepo.getContext(work));
      work.editions.add(edition);

      // create a supplier to generate volume IDs
      return new EditionMutatorImpl(edition, () -> idFactory.getNextId(PsqlWorkRepo.getContext(work, edition)));
   }

   @Override
   public EditionMutator editEdition(String id) throws NoSuchCatalogRecordException
   {
      for (EditionDV edition : work.editions) {
         if (edition.id.equals(id)) {
            // create a supplier to generate volume IDs
            return new EditionMutatorImpl(edition, () -> idFactory.getNextId(PsqlWorkRepo.getContext(work, edition)));
         }
      }

      throw new NoSuchCatalogRecordException("Unable to find edition with id [" + id + "].");
   }

   @Override
   public void removeEdition(String editionId) throws NoSuchCatalogRecordException
   {
      if (work.editions.isEmpty())
         throw new NoSuchCatalogRecordException("This work does not contain any editons.");

      for (EditionDV edition : work.editions)
      {
         if(edition.id.equals(editionId))
         {
            work.editions.remove(edition);
            return;
         }
      }

      throw new NoSuchCatalogRecordException("Could not find the edition [" + editionId + "]. The edition could not be removed.");
   }

   @Override
   public void removeVolume(String volumeId) throws NoSuchCatalogRecordException
   {
      if (work.editions.isEmpty())
         throw new NoSuchCatalogRecordException("This work does not contain any editons.");

      for (EditionDV edition : work.editions)
      {
         if (edition.volumes.isEmpty())
            throw new NoSuchCatalogRecordException("This edition does not contain any volumes.");

         for(VolumeDV volume : edition.volumes)
         {
            if(volume.id.equals(volumeId))
            {
               edition.volumes.remove(volume);
               return;
            }
         }
      }

      throw new NoSuchCatalogRecordException("Could not find the volume [" + volumeId + "]. The volume could not be removed.");
   }

   @Override
   public Future<String> execute()
   {
      Objects.requireNonNull(commitHook, "");

      return commitHook.apply(work);
   }

}
