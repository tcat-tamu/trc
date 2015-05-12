package edu.tamu.tcat.trc.entries.bio.solr;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.common.SolrInputDocument;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.tamu.tcat.catalogentries.events.dv.DateDescriptionDV;
import edu.tamu.tcat.catalogentries.events.dv.HistoricalEventDV;
import edu.tamu.tcat.trc.entries.bio.Person;
import edu.tamu.tcat.trc.entries.bio.dv.PersonDV;
import edu.tamu.tcat.trc.entries.bio.dv.PersonNameDV;
import edu.tamu.tcat.trc.entries.bio.rest.v1.SimplePersonResultDV;

public class PeopleSolrProxy
{
   private final static Logger logger = Logger.getLogger(PeopleSolrProxy.class.getName());

   private final static String personId = "id";
   private final static String familyName = "familyName";
   private final static String syntheticName = "syntheticName";
   private final static String displayName = "displayName";
   private final static String birthLocation = "birthLocation";
   private final static String birthDate = "birthDate";
   private final static String deathLocation = "deathLocation";
   private final static String deathDate = "deathDate";
   private final static String summary = "summary";

   private final static String personInfo = "personInfo";

   private SolrInputDocument document;
   private Map<String,Object> fieldModifier;
   private final static String SET = "set";

   public PeopleSolrProxy()
   {
      this.document = new SolrInputDocument();
   }

   public SolrInputDocument getDocument()
   {
      return document;
   }

   public static PeopleSolrProxy create(Person person)
   {
      PeopleSolrProxy proxy = new PeopleSolrProxy();
      PersonDV personDV = PersonDV.create(person);
      SimplePersonResultDV simplePerson = new SimplePersonResultDV(person);

      try
      {
         proxy.document.addField(personInfo, PeopleIndexingService.mapper.writeValueAsString(simplePerson));
      }
      catch (JsonProcessingException e)
      {
         logger.log(Level.SEVERE, "An error occurred when processing the SimplePersonResultDV to json" + e);
      }

      proxy.document.addField(personId, personDV.id);

      proxy.document.addField(syntheticName, constructSyntheticName(personDV.getAllNames()));

      PersonNameDV name = personDV.displayName;
      proxy.document.addField(familyName, guardNull(name.familyName));
      proxy.document.addField(displayName, guardNull(name.displayName));

      HistoricalEventDV birth = personDV.birth;
      proxy.document.addField(birthLocation, guardNull(birth.location));
      DateDescriptionDV bDate = birth.date;
      if (bDate != null)
         proxy.document.addField(birthDate, convertDate(bDate));

      HistoricalEventDV death = personDV.birth;
      proxy.document.addField(deathLocation, guardNull(death.location));
      if (death.date != null)
         proxy.document.addField(deathDate, convertDate(death.date));

      proxy.document.addField(summary, guardNull(personDV.summary));

      return proxy;
   }



   private static String guardNull(String value)
   {
      return value == null ? "" : value;
   }

   private static String convertDate(DateDescriptionDV date)
   {
      if (date.calendar != null)
         return date.calendar + "T00:00:00Z";
      return null;
   }

   /**
    * Constructs a synthetic name that contains the various values (title, first name,
    * family name, etc) from different names associated with this person. Each portion
    * of a person's name is collected into a set of 'name parts' that is then concatenated
    * to form a string-valued synthetic name. This allows all of the various name tokens to
    * be included in the search.
    *
    * @param names A set of names associated with a person.
    * @return A synthetic name that contains a union of the different name fields.
    */
   private static String constructSyntheticName(Set<PersonNameDV> names)
   {
      Set<String> nameParts = new HashSet<>();
      for(PersonNameDV name : names)
      {
         nameParts.add(name.title);
         nameParts.add(name.givenName);
         nameParts.add(name.middleName);
         nameParts.add(name.familyName);
      }

      StringBuilder sb = new StringBuilder();
      for (String part : nameParts)
      {
         if (part == null)
            continue;

         sb.append(part).append(" ");
      }

      return sb.toString().trim();
   }
}
