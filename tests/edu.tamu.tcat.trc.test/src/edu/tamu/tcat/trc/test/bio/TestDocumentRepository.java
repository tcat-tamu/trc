package edu.tamu.tcat.trc.test.bio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.trc.entries.types.bio.postgres.PeopleRepositoryImpl;
import edu.tamu.tcat.trc.entries.types.bio.repo.DateDescriptionMutator;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditPersonCommand;
import edu.tamu.tcat.trc.entries.types.bio.repo.HistoricalEventMutator;
import edu.tamu.tcat.trc.entries.types.bio.repo.PersonNameMutator;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.RestApiV1;
import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.common.HistoricalEvent;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;
import edu.tamu.tcat.trc.repo.NoSuchEntryException;
import edu.tamu.tcat.trc.test.TestUtils;

public class TestDocumentRepository
{
   private SqlExecutor exec;
   private ConfigurationProperties config;
   private PeopleRepositoryImpl repo;
   
   @Before
   public void setupTest() throws DataSourceException
   {
      IdFactoryProvider idFactoryProvider = TestUtils.makeIdFactoryProvider();

      config = TestUtils.loadConfigFile();
      exec = TestUtils.initPostgreSqlExecutor(config);
      
      this.repo = new PeopleRepositoryImpl();
      repo.setDatabaseExecutor(exec);
      repo.setIdFactory(idFactoryProvider);
      repo.activate();
   }

   @After
   public void tearDownTest() throws Exception
   {
      cleanDB();
      repo = null;
      exec = null;

      if (config instanceof SimpleFileConfigurationProperties)
         ((SimpleFileConfigurationProperties)config).dispose();
   }

   private void cleanDB() throws InterruptedException, ExecutionException
   {
      String sql = "DELETE FROM people";
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
      RestApiV1.Person personData = addRestAPIDataModel();
      String personId = createPerson(personData);
      Person person = repo.get(personId);
      validateData(personData, person);
   }
   
   @Test
   public void testEdit() throws InterruptedException, ExecutionException
   {
      RestApiV1.Person personData = addRestAPIDataModel();
      String personId = createPerson(personData);
      Person person = repo.get(personId);
      
      updatePerson(personId);
      Person personUpdated = repo.get(personId);
      
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
      RestApiV1.Person personData = addRestAPIDataModel();
      try
      {
         String personId = createPerson(personData);
         repo.delete(personId).get();
         repo.get(personId);
      }
      catch (NoSuchEntryException e)
      {
         assertFalse(false);
      }
      
   }
   
   private String createPerson(RestApiV1.Person personData) throws InterruptedException, ExecutionException
   {
      EditPersonCommand create = repo.create();
      
      PersonNameMutator personName = create.addName();
      personName.setDisplayName(personData.name.label);
      personName.setFamilyName(personData.name.familyName);
      personName.setGivenName(personData.name.givenName);
      personName.setMiddleName(personData.name.middleName);
      personName.setSuffix(personData.name.suffix);
      personName.setTitle(personData.name.title);
      
      
      PersonNameMutator altName1 = create.addNametoList();
      altName1.setDisplayName("Alternate Family Name 1");

      PersonNameMutator altName2 = create.addNametoList();
      altName2.setDisplayName("Alternate Family Name 2");
      
      PersonNameMutator altName3 = create.addNametoList();
      altName3.setDisplayName("Alternate Family Name 3");
      
      HistoricalEventMutator birthEvt = create.addBirthEvt();
      birthEvt.setTitle(personData.birth.title);
      birthEvt.setDescription(personData.birth.description);
      birthEvt.setLocations(personData.birth.location);
      
      DateDescriptionMutator birthDateDescription = birthEvt.addDateDescription();
      birthDateDescription.setCalendar(personData.birth.date.calendar);
      birthDateDescription.setDescription(personData.birth.date.description);
      
      HistoricalEventMutator deathEvt = create.addDeathEvt();
      deathEvt.setTitle(personData.death.title);
      deathEvt.setDescription(personData.death.description);
      deathEvt.setLocations(personData.death.location);
      
      DateDescriptionMutator deathDateDescription = deathEvt.addDateDescription();
      deathDateDescription.setCalendar(personData.death.date.calendar);
      deathDateDescription.setDescription(personData.death.date.description);
      
      create.setSummary("");
      
      return create.execute().get();
   }
   
   private void updatePerson(String personId) throws InterruptedException, ExecutionException
   {
      EditPersonCommand update = repo.update(personId);
      PersonNameMutator setPersonName = update.editName();
      setPersonName.setDisplayName("Changed The Name");
      setPersonName.setFamilyName("Name");
      setPersonName.setGivenName("Changed");
      setPersonName.setMiddleName("The");
      setPersonName.setSuffix("Sr.");
      setPersonName.setTitle("Title");
      
      update.clearNameList();
      
      PersonNameMutator altName1 = update.addNametoList();
      altName1.setDisplayName("Alternate Family Name 4");

      PersonNameMutator altName2 = update.addNametoList();
      altName2.setDisplayName("Alternate Family Name 5");
      
      HistoricalEventMutator editBirthEvt = update.editBirthEvt();
      editBirthEvt.setTitle("Birth Title");
      editBirthEvt.setLocations("United States");
      editBirthEvt.setDescription("adding a new description");
      
      DateDescriptionMutator birthDateDescription = editBirthEvt.editDateDescription();
      birthDateDescription.setCalendar("1825-01-10");
      birthDateDescription.setDescription("1825");
      
      HistoricalEventMutator editDeathEvt = update.editDeathEvt();
      editDeathEvt.setTitle("Birth Title");
      editDeathEvt.setLocations("United States");
      editDeathEvt.setDescription("adding a new description");
      
      DateDescriptionMutator deathDateDescription = editDeathEvt.editDateDescription();
      deathDateDescription.setCalendar("1903-12-25");
      deathDateDescription.setDescription("1903");
      
      update.setSummary("All items have been changed and or modified.");
      
      update.execute().get();
   }
   
   private void validateData(RestApiV1.Person data, Person person)
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
   
   private RestApiV1.Person addRestAPIDataModel()
   {
      RestApiV1.Person person = new RestApiV1.Person(); 
      
      person.name = new RestApiV1.PersonName();
      person.name.label = "George C. M. Douglas";
      person.name.givenName = "George";
      person.name.middleName = "Cunningham Monteath";
      person.name.familyName = "Douglas";
      person.name.title = "";
      person.name.role = "";
      person.name.suffix = "";
      
      person.altNames = new HashSet<RestApiV1.PersonName>();
      
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
