package edu.tamu.tcat.trc.test.entries.bibliographic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.ResourceNotFoundException;
import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.common.HistoricalEvent;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.entries.types.bio.impl.BiographicalEntryService;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.repo.DateDescriptionMutator;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditBiographicalEntryCommand;
import edu.tamu.tcat.trc.entries.types.bio.repo.HistoricalEventMutator;
import edu.tamu.tcat.trc.entries.types.bio.repo.PersonNameMutator;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.RestApiV1;
import edu.tamu.tcat.trc.test.support.TrcTestContext;

public class BioEntryRepoTests
{
   // FIXME need to ensure that we can pass in the table name (here and for the entries).
   //       -- this should be controlled through config

   private static final String ID_CONTEXT = "bio.entry.tests";
   private static final String TABLE_NAME = "bio_entry_tests";
   private static final String SCHEMA_DATA_FIELD = "data";

   private static TrcTestContext trcTestContext;

   private static TrcApplication ctx;
   private static BiographicalEntryService svc;

   @BeforeClass
   public static void beforeClass() throws DataSourceException
   {
      trcTestContext = new TrcTestContext();

      ctx = trcTestContext.getApplicationContext();

      svc = new BiographicalEntryService();
      svc.setTrcContext(ctx);
      svc.setRepoContext(trcTestContext.getRepoRegistrar());
      svc.activate();
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      trcTestContext.close();
      svc.dispose();
   }

   @Before
   public void setup()
   {

   }

   @After
   public void tearDown()
   {

   }

   private static void cleanDB() throws InterruptedException, ExecutionException
   {
      String sql = "DELETE FROM people";
      SqlExecutor exec = trcTestContext.getSqlExecutor();
      Future<Void> future = exec.submit((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            return null;
         }
      });

      future.get();
   }

   @Test
   public void testCreate() throws InterruptedException, ExecutionException
   {
      RestApiV1.Person personData = makeRestAPIDataModel();
      String personId = createPerson(personData);
      BiographicalEntryRepository repo = ctx.getRepository(null, BiographicalEntryRepository.class);
      BiographicalEntry person = repo.getOptionally(personId).orElseThrow(() -> new ResourceNotFoundException());
      validateData(personData, person);
   }

   @Test
   public void testEdit() throws InterruptedException, ExecutionException
   {
      BiographicalEntryRepository repo = ctx.getRepository(null, BiographicalEntryRepository.class);

      RestApiV1.Person personData = makeRestAPIDataModel();
      String personId = createPerson(personData);
      BiographicalEntry person = repo.getOptionally(personId).get();

      updatePerson(personId);
      BiographicalEntry personUpdated = repo.getOptionally(personId).get();

      assertEquals(person.getId(), personUpdated.getId());

      assertNotEquals(person.getCanonicalName().getDisplayName(), personUpdated.getCanonicalName().getDisplayName());
      assertNotEquals(person.getCanonicalName().getFamilyName(), personUpdated.getCanonicalName().getFamilyName());
      assertNotEquals(person.getCanonicalName().getGivenName(), personUpdated.getCanonicalName().getGivenName());
      assertNotEquals(person.getCanonicalName().getMiddleName(), personUpdated.getCanonicalName().getMiddleName());
      assertNotEquals(person.getCanonicalName().getSuffix(), personUpdated.getCanonicalName().getSuffix());
      assertNotEquals(person.getCanonicalName().getTitle(), personUpdated.getCanonicalName().getTitle());

      assertNotEquals(person.getBirth().getDate().getCalendar(), personUpdated.getBirth().getDate().getCalendar());
      assertNotEquals(person.getBirth().getDate().getDescription(), personUpdated.getBirth().getDate().getDescription());
      assertNotEquals(person.getBirth().getDescription(), personUpdated.getBirth().getDescription());
      assertNotEquals(person.getBirth().getLocation(), personUpdated.getBirth().getLocation());
      assertNotEquals(person.getBirth().getTitle(), personUpdated.getBirth().getTitle());

      assertNotEquals(person.getDeath().getDate().getCalendar(), personUpdated.getDeath().getDate().getCalendar());
      assertNotEquals(person.getDeath().getDate().getDescription(), personUpdated.getDeath().getDate().getDescription());
      assertNotEquals(person.getDeath().getDescription(), personUpdated.getDeath().getDescription());
      assertNotEquals(person.getDeath().getLocation(), personUpdated.getDeath().getLocation());
      assertNotEquals(person.getDeath().getTitle(), personUpdated.getDeath().getTitle());

      assertNotEquals(person.getNames(), personUpdated.getNames());
      assertNotEquals(person.getSummary(), personUpdated.getSummary());

   }

   @Test
   public void testDelete() throws InterruptedException, ExecutionException
   {
      RestApiV1.Person personData = makeRestAPIDataModel();
      BiographicalEntryRepository repo = ctx.getRepository(null, BiographicalEntryRepository.class);
      String personId = createPerson(personData);
      repo.remove(personId).get();

      Optional<BiographicalEntry> entry = repo.getOptionally(personId);
      assertFalse(entry.isPresent());
   }

   @SuppressWarnings("deprecation")
   private String createPerson(RestApiV1.Person personData) throws InterruptedException, ExecutionException
   {
      BiographicalEntryRepository repo = ctx.getRepository(null, BiographicalEntryRepository.class);
      EditBiographicalEntryCommand command = repo.create();

      PersonNameMutator personName = command.editCanonicalName();
      personName.setDisplayName(personData.name.label);
      personName.setFamilyName(personData.name.familyName);
      personName.setGivenName(personData.name.givenName);
      personName.setMiddleName(personData.name.middleName);
      personName.setSuffix(personData.name.suffix);
      personName.setTitle(personData.name.title);


      PersonNameMutator altName1 = command.addAlternateName();
      altName1.setDisplayName("Alternate Family Name 1");
      altName1.setFamilyName("Family Name");
      altName1.setGivenName("Given Name");
      altName1.setMiddleName("Middle Name");
      altName1.setSuffix("Suffix Name");
      altName1.setTitle("Title");

      PersonNameMutator altName2 = command.addAlternateName();
      altName2.setDisplayName("Alternate Family Name 2");

      PersonNameMutator altName3 = command.addAlternateName();
      altName3.setDisplayName("Alternate Family Name 3");

      RestApiV1.HistoricalEvent birth = personData.birth;
      HistoricalEventMutator birthEvt = command.editBirth();
      birthEvt.setTitle(birth.title);
      birthEvt.setDescription(birth.description);
      birthEvt.setLocation(birth.location);

      DateDescriptionMutator birthDateDescription = birthEvt.editDate();
      birthDateDescription.setCalendar(LocalDate.parse(birth.date.calendar));
      birthDateDescription.setDescription(birth.date.description);

      RestApiV1.HistoricalEvent death = personData.death;
      HistoricalEventMutator deathEvt = command.editDeath();
      deathEvt.setTitle(death.title);
      deathEvt.setDescription(death.description);
      deathEvt.setLocation(death.location);

      DateDescriptionMutator deathDateDescription = deathEvt.editDate();
      deathDateDescription.setCalendar(LocalDate.parse(death.date.calendar));
      deathDateDescription.setDescription(death.date.description);

      command.setSummary("");

      return command.execute().get();
   }

   @SuppressWarnings("deprecation")
   private void updatePerson(String personId) throws InterruptedException, ExecutionException
   {
      BiographicalEntryRepository repo = ctx.getRepository(null, BiographicalEntryRepository.class);

      EditBiographicalEntryCommand update = repo.edit(personId);
      PersonNameMutator setPersonName = update.editCanonicalName();
      setPersonName.setDisplayName("Changed The Name");
      setPersonName.setFamilyName("Name");
      setPersonName.setGivenName("Changed");
      setPersonName.setMiddleName("The");
      setPersonName.setSuffix("Sr.");
      setPersonName.setTitle("Title");

      update.clearAlternateNames();;

      PersonNameMutator altName1 = update.addAlternateName();
      altName1.setDisplayName("Alternate Family Name 4");

      PersonNameMutator altName2 = update.addAlternateName();
      altName2.setDisplayName("Alternate Family Name 5");

      HistoricalEventMutator editBirthEvt = update.editBirth();
      editBirthEvt.setTitle("Birth Title");
      editBirthEvt.setLocation("United States");
      editBirthEvt.setDescription("adding a new description");

      DateDescriptionMutator birthDateDescription = editBirthEvt.editDate();
      birthDateDescription.setCalendar(LocalDate.parse("1825-01-10"));
      birthDateDescription.setDescription("1825");

      HistoricalEventMutator editDeathEvt = update.editDeath();
      editDeathEvt.setTitle("Birth Title");
      editDeathEvt.setLocation("United States");
      editDeathEvt.setDescription("adding a new description");

      DateDescriptionMutator deathDateDescription = editDeathEvt.editDate();
      deathDateDescription.setCalendar(LocalDate.parse("1903-12-25"));
      deathDateDescription.setDescription("1903");

      update.setSummary("All items have been changed and or modified.");

      update.execute().get();
   }

   private void validateData(RestApiV1.Person data, BiographicalEntry person)
   {
      validatePersonName(person.getCanonicalName(), data.name);
      validateEvent(person.getBirth(), data.birth);
      validateEvent(person.getDeath(), data.death);
      assertEquals(data.summary, person.getSummary());
   }

   private void validatePersonName(PersonName name, RestApiV1.PersonName dto)
   {
      assertEquals(dto.label, name.getDisplayName());
      assertEquals(dto.familyName, name.getFamilyName());
      assertEquals(dto.givenName, name.getGivenName());
      assertEquals(dto.middleName, name.getMiddleName());
      assertEquals(dto.suffix, name.getSuffix());
      assertEquals(dto.title, name.getTitle());
   }

   private void validateEvent(HistoricalEvent event, RestApiV1.HistoricalEvent dto)
   {
      assertEquals(event.getTitle(), dto.title);
      assertEquals(event.getDescription(), dto.description);
      assertEquals(event.getLocation(), dto.location);

      DateDescription dateDescription = event.getDate();
      assertEquals(dateDescription.getDescription(), dto.date.description);
      assertEquals(dateDescription.getCalendar().toString(), dto.date.calendar);
   }

   private RestApiV1.Person makeRestAPIDataModel()
   {
      RestApiV1.Person person = new RestApiV1.Person();

      person.name = new RestApiV1.PersonName();
      person.name.label = "George C. M. Douglas";
      person.name.givenName = "George";
      person.name.middleName = "Cunningham Monteath";
      person.name.familyName = "Douglas";
      person.name.title = "Mr.";
      person.name.role = "Role";
      person.name.suffix = "III";

      person.altNames = new HashSet<>();

      person.birth = new RestApiV1.HistoricalEvent();
      person.birth.title = "birth";
      person.birth.description = "";
      person.birth.location = "";
      person.birth.date = new RestApiV1.DateDescription();
      person.birth.date.calendar = "1826-01-01";
      person.birth.date.description = "1826";

      person.death = new RestApiV1.HistoricalEvent();
      person.death.title = "death";
      person.death.description = "";
      person.death.location = "";
      person.death.date = new RestApiV1.DateDescription();
      person.death.date.calendar = "1904-01-01";
      person.death.date.description = "1904";

      person.summary = "";

      return person;
   }
}
