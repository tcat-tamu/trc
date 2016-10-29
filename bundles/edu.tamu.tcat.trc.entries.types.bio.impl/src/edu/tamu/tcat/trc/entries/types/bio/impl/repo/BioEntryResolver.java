package edu.tamu.tcat.trc.entries.types.bio.impl.repo;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.common.HistoricalEvent;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditBiographicalEntryCommand;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.resolver.InvalidReferenceException;

public class BioEntryResolver extends EntryResolverBase<BiographicalEntry>
{
   private final BasicRepoDelegate<BiographicalEntry, DataModelV1.Person, EditBiographicalEntryCommand> delegate;

   public BioEntryResolver(ConfigurationProperties config, BasicRepoDelegate<BiographicalEntry, DataModelV1.Person, EditBiographicalEntryCommand> delegate)
   {
      super(BiographicalEntry.class, config, BiographicalEntryRepository.ENTRY_URI_BASE, BiographicalEntryRepository.ENTRY_TYPE_ID);
      this.delegate = delegate;
   }

   @Override
   public BiographicalEntry resolve(Account account, EntryId reference) throws InvalidReferenceException
   {
      if (!accepts(reference))
         throw new InvalidReferenceException(reference, "Unsupported reference type.");

      return delegate.get(account, reference.getId());
   }

   @Override
   protected String getId(BiographicalEntry person)
   {
      return person.getId();
   }

   @Override
   public String getLabel(BiographicalEntry person)
   {
      return MessageFormat.format("{0} ({1}-{2})",
            formatName(person),
            formatYear(person.getBirth()),
            formatYear(person.getDeath()));
   }

   @Override
   public String getHtmlLabel(BiographicalEntry person)
   {
      String template = "<span class=\"bioentry-label\"><span class=\"bioentry-name\">{0}</span> <span class=\"bioentry-lifespan\">({1}-{2})</span></span>";
      return MessageFormat.format(template,
            formatName(person),
            formatYear(person.getBirth()),
            formatYear(person.getDeath()));
   }

   private String formatYear(HistoricalEvent evt)
   {
      LocalDate date = null;
      if (evt != null && evt.getDate() != null)
         date = evt.getDate().getCalendar();

      return (date == null) ? "?" : String.valueOf(date.getYear());
   }

   private String formatName(BiographicalEntry person)
   {
      String displayName = "unnamed";
      PersonName name = getDisplayName(person);
      if (name != null) {
         displayName = name.getDisplayName();
         if (displayName == null) {
            displayName = MessageFormat.format("{0} {1}",
                  guardNull(name.getGivenName()),
                  guardNull(name.getFamilyName()));
         }
      }
      return displayName.trim();
   }

   /**
    * Gets a display name for a person
    *
    * @param person
    * @return
    */
   private PersonName getDisplayName(BiographicalEntry person)
   {
      // use canonical name by default
      PersonName name = person.getCanonicalName();

      // fall back to first element of alternate names
      if (name == null) {
         Set<? extends PersonName> names = person.getAlternativeNames();
         if (!names.isEmpty())
            name = names.iterator().next();
      }

      return name;
   }

   private static String guardNull(String value)
   {
      String result = value == null ? "" : value;
      return result.trim();
   }

   @Override
   public CompletableFuture<Boolean> remove(Account account, EntryId reference)
   {
      return delegate.remove(account, reference.getId());
   }
}