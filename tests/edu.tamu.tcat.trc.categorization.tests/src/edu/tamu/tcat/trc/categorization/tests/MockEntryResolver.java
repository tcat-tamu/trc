package edu.tamu.tcat.trc.categorization.tests;

import static java.text.MessageFormat.format;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.resolver.EntryResolver;
import edu.tamu.tcat.trc.resolver.InvalidReferenceException;

class MockEntryResolver implements EntryResolver<MockEntry>
{
   private static final String TYPE = "trc.tests.mock_entry";
   private static final String API_RESOURCE_PATH = "tests/mockentries/";

   private final URI baseUrl = URI.create("http://example.com/api");
   private final Map<String, MockEntry> entries = new HashMap<>();


   public MockEntry create(String description)
   {
      MockEntry result = new MockEntry(description);
      entries.put(result.getId(), result);
      return result;
   }

   @Override
   public MockEntry resolve(Account account, EntryReference reference) throws InvalidReferenceException
   {
      String errUnsupportedType = "Unsupported reference type {0}";
      String errNotFound = "Cannot find mock entry with id = {0}";
      if (!TYPE.equals(reference.type))
         throw new InvalidReferenceException(reference, format(errUnsupportedType, reference.type));

      if (!entries.containsKey(reference.id))
         throw new InvalidReferenceException(reference, format(errNotFound, reference.id));

      return entries.get(reference.id);
   }

   @Override
   public URI toUri(EntryReference reference) throws InvalidReferenceException
   {
      return baseUrl.resolve(API_RESOURCE_PATH).resolve(reference.id);
   }

   @Override
   public EntryReference makeReference(MockEntry instance) throws InvalidReferenceException
   {
      EntryReference reference = new EntryReference();
      reference.id = instance.id;
      reference.type = TYPE;

      return reference;
   }

   @Override
   public EntryReference makeReference(URI uri) throws InvalidReferenceException
   {
      URI relEntryUri = baseUrl.resolve(API_RESOURCE_PATH).relativize(uri);
      if (uri.equals(relEntryUri))
         throw new InvalidReferenceException(uri, format("Expected URL at endpoint {0}", baseUrl.resolve(API_RESOURCE_PATH)));

      EntryReference reference = new EntryReference();
      reference.id = relEntryUri.getPath();
      reference.type = TYPE;

      return reference;
   }

   @Override
   public boolean accepts(Object obj)
   {
      return (obj instanceof MockEntry);
   }

   @Override
   public boolean accepts(EntryReference reference)
   {
      return TYPE.equals(reference.type);
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