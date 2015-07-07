package edu.tamu.tcat.trc.resources.books.hathitrust;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.tamu.tcat.hathitrust.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.bibliography.BasicRecordIdentifier;
import edu.tamu.tcat.hathitrust.bibliography.Item;
import edu.tamu.tcat.hathitrust.bibliography.Record;
import edu.tamu.tcat.hathitrust.bibliography.Record.IdType;
import edu.tamu.tcat.hathitrust.client.v1.basic.BibAPIClientImpl;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.resources.books.resolve.CopyResolverStrategy;
import edu.tamu.tcat.trc.resources.books.resolve.ResourceAccessException;

public class HathiTrustCopyResolver implements CopyResolverStrategy<HathiTrustCopy>
{
   // Uses the HathiTrust SDK to construct references to DigitalCopies.
   // Initially, all we need are fairly simple links to enable users to read the book and
   // basic metadata. We can/will add additional support as needed (e.g., access to full text for indexing)

   private static final Pattern copyIdPattern = Pattern.compile("^htid:(\\d{9}#(.*)$");
   public static final String HATHI_TRUST = "edu.tamu.tcat.hathitrust.api_endpoint";      // TODO change to org.hathitrust.api_endpoint

   private static final String identPattern = "^htid:[0-9]{9}$";
   private static final Pattern p = Pattern.compile(identPattern);

   private BibAPIClientImpl bibClient;
   private ConfigurationProperties config;

   public HathiTrustCopyResolver()
   {
   }

   public void setConfig(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void activate()
   {
      if (config == null)
         throw new IllegalStateException("Activation failed. Configuration properties not available.");

      String url = config.getPropertyValue(HATHI_TRUST, String.class);
      if (url == null || url.trim().isEmpty())
         throw new IllegalStateException("Activation failed. No API endpoint supplied. Expected configuration property for  [" + HATHI_TRUST + "].");

      bibClient = BibAPIClientImpl.create(url);
   }

   public void dispose()
   {
      bibClient.close();
   }

   @Override
   public boolean canResolve(String identifier)
   {
      if(identifier == null )
         return false;

      Matcher m = p.matcher(identifier.substring(0, 14));
      return  m.matches() ;

   }

   /**
    * @param identifier Will be in the format {@code htid:<recordnumber>#itemId}.
    */
   @Override
   public HathiTrustCopy resolve(String identifier) throws ResourceAccessException, IllegalArgumentException
   {
      if (!canResolve(identifier))
         throw new IllegalArgumentException("Unrecognized identifier format [" + identifier + "]");

      Matcher matcher = copyIdPattern.matcher(identifier);
      if (!matcher.find())
         throw new IllegalArgumentException("Unrecognized identifier format [" + identifier + "]");

      String itemId = matcher.group(2);

      BasicRecordIdentifier recordId = new BasicRecordIdentifier(IdType.RECORDNUMBER, matcher.group(1));

      // Create a pattern to get the record number our of the identifier.
      try
      {
         Record record = getRecord(recordId);
         Item item = record.getItem(itemId);

         // FIXME not a copy. This is a record.
         throw new UnsupportedOperationException();
//         return new HathiTrustCopy(record, item);

      }
      catch (HathiTrustClientException e)
      {
         throw new ResourceAccessException("A message to our readers", e);
      }
   }


   private Record getRecord(BasicRecordIdentifier recordId) throws HathiTrustClientException, ResourceAccessException
   {
      Collection<Record> records = bibClient.lookup(recordId);
      Record record = records.stream().findFirst().orElse(null);
      if (record == null)
         throw new ResourceAccessException("Record not found [" + recordId +"]");
      return record;
   }
}
