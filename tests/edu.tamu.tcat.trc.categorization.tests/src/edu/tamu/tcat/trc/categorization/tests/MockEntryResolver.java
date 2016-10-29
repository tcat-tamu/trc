package edu.tamu.tcat.trc.categorization.tests;

import static java.text.MessageFormat.format;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolver;
import edu.tamu.tcat.trc.resolver.InvalidReferenceException;

class MockEntryResolver implements EntryResolver<MockEntry>
{
   private static final String TYPE = "trc.tests.mock_entry";
   private static final String API_RESOURCE_PATH = "tests/mockentries/";

   private final URI baseUrl = URI.create("http://example.com/api");
   private final Map<String, MockEntry> entries = new HashMap<>();

   @Override
   public Class<MockEntry> getType()
   {
      return MockEntry.class;
   }

   @Override
   public String getLabel(MockEntry instance)
   {
      return "Mock Entry";
   }

   public MockEntry create(String description)
   {
      MockEntry result = new MockEntry(description);
      entries.put(result.getId(), result);
      return result;
   }

   @Override
   public MockEntry resolve(Account account, EntryId reference) throws InvalidReferenceException
   {
      String errUnsupportedType = "Unsupported reference type {0}";
      String errNotFound = "Cannot find mock entry with id = {0}";
      if (!TYPE.equals(reference.getType()))
         throw new InvalidReferenceException(reference, format(errUnsupportedType, reference.getType()));

      if (!entries.containsKey(reference.getId()))
         throw new InvalidReferenceException(reference, format(errNotFound, reference.getId()));

      return entries.get(reference.getId());
   }

   @Override
   public URI toUri(EntryId reference) throws InvalidReferenceException
   {
      return baseUrl.resolve(API_RESOURCE_PATH).resolve(reference.getId());
   }

   @Override
   public EntryId makeReference(MockEntry instance) throws InvalidReferenceException
   {
      return new EntryId(instance.getId(), TYPE);
   }

   @Override
   public EntryId makeReference(URI uri) throws InvalidReferenceException
   {
      URI relEntryUri = baseUrl.resolve(API_RESOURCE_PATH).relativize(uri);
      if (uri.equals(relEntryUri))
         throw new InvalidReferenceException(uri, format("Expected URL at endpoint {0}", baseUrl.resolve(API_RESOURCE_PATH)));

      return new EntryId(relEntryUri.getPath(), TYPE);
   }

   @Override
   public boolean accepts(Object obj)
   {
      return (obj instanceof MockEntry);
   }

   @Override
   public boolean accepts(EntryId reference)
   {
      return TYPE.equals(reference.getType());
   }

   @Override
   public boolean accepts(URI uri)
   {
      URI relEntryUri = baseUrl.resolve(API_RESOURCE_PATH).relativize(uri);
      if (uri.equals(relEntryUri))
         return false;

      // may need additional tests
      return true;
   }

}