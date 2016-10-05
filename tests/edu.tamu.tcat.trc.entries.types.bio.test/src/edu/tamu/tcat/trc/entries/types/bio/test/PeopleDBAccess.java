package edu.tamu.tcat.trc.entries.types.bio.test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.trc.entries.common.HistoricalEvent;
import edu.tamu.tcat.trc.entries.types.biblio.dto.DateDescriptionDTO;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.id.IdFactory;
import edu.tamu.tcat.trc.repo.id.IdFactoryProvider;
import edu.tamu.tcat.trc.test.ClosableSqlExecutor;
import edu.tamu.tcat.trc.test.TestUtils;

public class PeopleDBAccess
{
   private ClosableSqlExecutor exec;
   private ConfigurationProperties config;

   private PsqlPeopleRepo repo;
   private IdFactory idFactory;

   @Before
   public void setupTest() throws DataSourceException
   {
      IdFactoryProvider idFactoryProvider = TestUtils.makeIdFactoryProvider();
      idFactory = idFactoryProvider.getIdFactory(PsqlPeopleRepo.ID_CONTEXT);

      config = TestUtils.loadConfigFile();
      exec = TestUtils.initPostgreSqlExecutor(config);

      this.repo = new PsqlPeopleRepo();
      repo.setDatabaseExecutor(exec);
      repo.setIdFactory(idFactoryProvider);
      repo.activate();
   }

   @After
   public void tearDownTest() throws Exception
   {
//      String sql = "DELETE FROM people";
//      Future<Void> future = exec.submit((conn) -> {
//         try (PreparedStatement ps = conn.prepareStatement(sql)) {
//            ps.executeUpdate();
//            return null;
//         }
//      });

//      future.get();
      repo.dispose();

      exec.close();

      if (config instanceof SimpleFileConfigurationProperties)
         ((SimpleFileConfigurationProperties)config).dispose();
   }

   private DateDescriptionDTO makeHistoricalDate(String desc, LocalDate calendar)
   {
      DateDescriptionDTO date = new DateDescriptionDTO();
      date.calendar = calendar.toString();
      date.description = desc;

      return date;
   }

   private HistoricalEventDTO makeHistoricalEvent(String title, String description, String location,
                                                  DateDescriptionDTO date)
   {
      HistoricalEventDTO event = new HistoricalEventDTO();

      event.id = UUID.randomUUID().toString();
      event.title = title;
      event.description = description;
      event.location = location;
      event.date = date;

      return event;
   }

//   @Test
   public void testCreateWithDTO() throws Exception
   {
      // tests the low-level data creation API.
      PersonDTO dto = new PersonDTO();
      dto.id = idFactory.get();
      dto.displayName = new PersonNameDTO("Test", null, "User");
      dto.birth = makeHistoricalEvent(
            "Birth date of " + dto.displayName,
            "The date that this person was born.",
            "Paris, France",
            makeHistoricalDate("A long time ago", LocalDate.of(1956, Month.APRIL, 23)));
      dto.death = makeHistoricalEvent(
            "Death date of " + dto.displayName,
            "The date that this person was died.",
            null,
            null);
      dto.names = new HashSet<>();
      dto.names.add(new PersonNameDTO("Alternate", null, "Name"));
      dto.summary = "A simple person description for testing purposes.";

      Future<String> future = repo.create(dto);

      String id = future.get();

      BiographicalEntry person = repo.get(id);
      PersonName name = person.getCanonicalName();

      assertEquals("ids do not match", person.getId(), dto.id);
      assertEquals("summaries do not match", person.getSummary(), dto.summary);
      checkName(name, dto.displayName);

      checkDate(person.getBirth(), dto.birth);
      checkDate(person.getDeath(), dto.death);

      // check alternative names -- TODO build a map of id -> name, use that to compare
      PersonNameDTO[] altNamesDto = new PersonNameDTO[dto.names.size()];
      PersonName[] altNames = new PersonName[person.getAlternativeNames().size()];
      checkName(person.getAlternativeNames().toArray(altNames)[0],
                dto.names.toArray(altNamesDto)[0]);
   }

//   @Test
   public void testLoadSurnamesFile() throws Exception
   {
//      PersonNameGenerator generator = new PersonNameGenerator(config, 0.48);
////      CensusSurnameGenerator surnames = new CensusSurnameGenerator(config, Ethnicity.All);
////      USBabyNamesGenerator babynames = new USBabyNamesGenerator(config, 2014, 1);
//      IntStream.range(1, 100_000)
//               .parallel()
//               .mapToObj(ix -> generator.next().displayName)
//               .forEach(System.out::println);

   }

   @Test
   public void testGetAll() throws RepositoryException
   {
      Iterator<BiographicalEntry> peopleIterator = repo.listAll();
      while (peopleIterator.hasNext())
      {
         BiographicalEntry person = peopleIterator.next();
      }
   }
   private void checkDate(HistoricalEvent death, HistoricalEventDTO dto)
   {
      // FIXME implement this.
      if ((death == null) || (dto == null))
      {
         assertEquals(death, dto);
         return;
      }

      death.getId();
      death.getTitle();
      death.getDescription();
      death.getLocation();
      death.getDate();
      // TODO Auto-generated method stub

   }

   private void checkName(PersonName name, PersonNameDTO dto)
   {
      assertEquals("family names not match", name.getFamilyName(), dto.familyName);
      assertEquals("family names not match", name.getMiddleName(), dto.middleName);
      assertEquals("family names not match", name.getGivenName(), dto.givenName);
   }

}
