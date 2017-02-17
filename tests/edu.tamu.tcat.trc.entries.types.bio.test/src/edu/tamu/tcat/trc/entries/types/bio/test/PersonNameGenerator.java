package edu.tamu.tcat.trc.entries.types.bio.test;

import java.text.MessageFormat;

import org.apache.commons.math3.random.RandomDataGenerator;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.types.bio.test.CensusSurnameGenerator.Ethnicity;

public class PersonNameGenerator
{

   private final RandomDataGenerator random = new RandomDataGenerator();

   private CensusSurnameGenerator surnames;
   private USBabyNamesGenerator givenNames;

   private double percentMale;
   private double percentMiddle = 0.83;


   public PersonNameGenerator(ConfigurationProperties config, double percentMale)
   {
      this.percentMale = percentMale;
      if (percentMale < 0 || percentMale > 1)
         throw new IllegalArgumentException(
               MessageFormat.format("percentMale must be in the range of [0, 1]. {0} supplied.", percentMale));

      try
      {
         surnames = new CensusSurnameGenerator(config, Ethnicity.All);
         givenNames = new USBabyNamesGenerator(config, 2014, 1);
      }
      catch (NameGeneratorException e)
      {
         throw new IllegalStateException(e);
      }
   }

   public PersonNameDTO next()
   {
      return random.nextUniform(0, 1) < percentMale ? nextMale() : nextFemale();
   }

   public PersonNameDTO nextMale()
   {
      PersonNameDTO dto = new PersonNameDTO();
      dto.familyName = surnames.next();
      dto.givenName = givenNames.nextMale();

      if (random.nextUniform(0, 1) < percentMiddle)
         dto.middleName = givenNames.nextMale();

      // add title, suffix
      double titleSelector = random.nextUniform(0, 1);
      if (titleSelector < 0.016)
         dto.title = "Dr.";
      else if (titleSelector < 0.4)
         dto.title = "Mr.";

      dto.displayName = makeDisplayName(dto);
      return dto;
   }

   public PersonNameDTO nextFemale()
   {
      PersonNameDTO dto = new PersonNameDTO();
      dto.familyName = surnames.next();
      dto.givenName = givenNames.nextFemale();

      if (random.nextUniform(0, 1) < percentMiddle)
         dto.middleName = givenNames.nextFemale();

      // add title, suffix
      double titleSelector = random.nextUniform(0, 1);
      if (titleSelector < 0.007)
         dto.title = "Dr.";
      else if (titleSelector < 0.3)
         dto.title = "Ms";
      else if (titleSelector < 0.4)
         dto.title = "Mrs.";

      dto.displayName = makeDisplayName(dto);
      return dto;
   }

   private String makeDisplayName(PersonNameDTO dto)
   {
      StringBuilder sb = new StringBuilder();
      if (dto.title != null)
         sb.append(dto.title).append(" ");
      sb.append(dto.givenName).append(" ");
      if (dto.middleName != null)
         sb.append(dto.middleName).append(" ");
      sb.append(dto.familyName);

      return sb.toString();
   }
}
