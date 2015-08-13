package edu.tamu.tcat.trc.entries.types.bio.test.names;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.types.bio.test.NameGeneratorException;
import edu.tamu.tcat.trc.entries.types.bio.test.WeightedObservationHeapSampler;

public class USBabyNamesGenerator
{
   // TODO implement an interface and allow different types of surname generators to be created.

   private static final String FILE_PARSE_ERROR = "Failed to read data from file [{0}]";

   /* Path to directory in which baby names are stored. */
   private static final String BABY_NAMES_PATH = "ssa.babynames.path";

   private final RandomDataGenerator random = new RandomDataGenerator();
   private NameSampler males;
   private NameSampler females;

   private final double percentMale;

   public USBabyNamesGenerator(ConfigurationProperties config, int year, double percentMale)
         throws NameGeneratorException
   {
      this.percentMale = percentMale;
      loadCensusData(config, year);
   }

   private void loadCensusData(ConfigurationProperties config, int year) throws NameGeneratorException
   {
      Path dirPath = config.getPropertyValue(BABY_NAMES_PATH, Path.class);
      Path fpath = dirPath.resolve(MessageFormat.format("yob{0}.txt", String.valueOf(year)));

      try
      {
         CsvMapper mapper = new CsvMapper();
         CsvSchema schema = mapper.schemaFor(BabyName.class);

         mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
         MappingIterator<BabyName> it = mapper.reader(BabyName.class)
               .with(schema)
               .readValues(fpath.toFile());

         List<BabyName> records = new ArrayList<>();
         while (it.hasNext()) {
            records.add(it.next());
         }

         males = new NameSampler(filterBySex(records, "M"));
         females = new NameSampler(filterBySex(records, "F"));
      }
      catch (IOException ex)
      {
         throw new NameGeneratorException(MessageFormat.format(FILE_PARSE_ERROR, fpath), ex);
      }
   }

   private static List<BabyName> filterBySex(List<BabyName> records, String sex)
   {
      return records.parallelStream()
                    .filter(n -> n.sex.equals(sex))
                    .collect(Collectors.toList());
   }

   public String next()
   {
      double selector = random.nextUniform(0, 1);
      return (selector < percentMale) ? males.next() : females.next();
   }

   public String nextMale()
   {
      return males.next();
   }

   public String nextFemale()
   {
      return females.next();
   }

   @JsonPropertyOrder({ "name", "sex", "count"})
   public static class BabyName
   {
      public String name;
      public String sex;
      public int count;
   }

   private static class NameSampler
   {
      private int size = 1000;
      private WeightedObservationHeapSampler<BabyName> sampler;
      private Iterator<String> it;

      public NameSampler(List<BabyName> records)
      {
         this.sampler = new WeightedObservationHeapSampler<>(records, (n) -> n.count);
         refresh();
      }

      private void refresh()
      {
         List<BabyName> samples = sampler.sampleWithReplacement(size);
         List<String> names = samples.parallelStream().map((s) -> s.name).collect(Collectors.toList());
         this.it = names.iterator();

         size = size * 2;
      }

      public synchronized String next()
      {
         if (!it.hasNext())
            refresh();

         return it.next();
      }

   }
}
