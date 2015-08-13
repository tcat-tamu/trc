package edu.tamu.tcat.trc.entries.types.bio.test.names;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.WordUtils;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.types.bio.test.NameGeneratorException;
import edu.tamu.tcat.trc.entries.types.bio.test.WeightedObservationHeapSampler;

public class CensusSurnameGenerator
{
   // TODO implement an interface and allow different types of surname generators to be created.

   private static final String FILE_PARSE_ERROR = "Failed to read data from file [{0}]";
   private static final String CENSUS_SURNAMES_PATH = "gov.census.data.surnames.path";

   public enum Ethnicity
   {
      All(null),
      White(name -> name.pctwhite),
      Black(name -> name.pctblack),
      Hispanic(name -> name.pcthispanic),
      Multiple(name -> name.pct2prace),
      AsiaPacificIslander(name -> name.pctapi),
      AmericanIndianAlaskaNative(name -> name.pctaian);

      private static Pattern dblRegEx = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
      private Function<CensusSurname, String> pct;
      private Ethnicity(Function<CensusSurname, String> pct)
      {
         this.pct = pct;
      }

      public double getWeight(CensusSurname record)
      {
         if (pct == null)
            return record.count;

         String value = pct.apply(record);
         return dblRegEx.matcher(value).matches() ? record.count * Double.parseDouble(value) : 0;
      }
   }


   private final WeightedObservationHeapSampler<CensusSurname> sampler;
   private int size = 1000;
   private Iterator<String> iterator;



   public CensusSurnameGenerator(ConfigurationProperties config, Ethnicity ethnicity)
         throws NameGeneratorException
   {
      List<CensusSurname> records = loadCensusData(config);
      sampler = new WeightedObservationHeapSampler<>(records, ethnicity::getWeight);


      sampleNames();
   }

   private List<CensusSurname> loadCensusData(ConfigurationProperties config) throws NameGeneratorException
   {
      Path csvPath = config.getPropertyValue(CENSUS_SURNAMES_PATH, Path.class);
      try
      {
         CsvMapper mapper = new CsvMapper();
         CsvSchema schema = mapper.schemaFor(CensusSurname.class).withHeader();

         mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
         MappingIterator<CensusSurname> it = mapper.reader(CensusSurname.class)
               .with(schema)
               .readValues(csvPath.toFile());

         List<CensusSurname> records = new ArrayList<>();
         while (it.hasNext()) {
            records.add(it.next());
         }
         return records;
      }
      catch (IOException ex)
      {
         throw new NameGeneratorException(MessageFormat.format(FILE_PARSE_ERROR, csvPath), ex);
      }
   }


   private void sampleNames()
   {
      List<CensusSurname> samples = sampler.sampleWithReplacement(size);
      List<String> names = samples.parallelStream().map((s) -> s.name).collect(Collectors.toList());
      this.iterator = names.iterator();

      size = size * 2;
   }

   public synchronized String next()
   {
      if (!iterator.hasNext())
         sampleNames();

      String name = iterator.next();
      return WordUtils.capitalize(name.toLowerCase());
   }

   @JsonPropertyOrder
   public static class CensusSurname
   {
      public String name;
      public int rank;
      public int count;
      public double prop100k;
      public double cum_prop;
      public String pctwhite;    // White
      public String pctblack;    // Black
      public String pctapi;      // Asia Pacific Islander
      public String pctaian;     // American Indian and Alaska Native
      public String pct2prace;   // Two or more races
      public String pcthispanic; // Hispanic

   }
}
