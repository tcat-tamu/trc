package edu.tamu.tcat.trc.entries.types.bio.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import edu.tamu.tcat.trc.entries.types.bio.test.CensusSurnameGenerator.CensusSurname;


public class TestObservationSampler
{
   String surnameFile = "D:\\data\\us-names\\census\\surnames\\2010\\Names_2010Census.csv";

   // Surnames: http://www.census.gov/topics/population/genealogy/data/2010_surnames.html
   // Lookup By State: https://www.census.gov/geo/maps-data/data/nlt.html
   // BabyNames: https://www.ssa.gov/oact/babynames/
   public TestObservationSampler()
   {
      // TODO Auto-generated constructor stub
   }


   @Test
   public void testLoadSurnamesFile() throws JsonProcessingException, IOException
   {
      CsvMapper mapper = new CsvMapper();
      CsvSchema schema = mapper.schemaFor(CensusSurname.class).withHeader();

      mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
      Path csvPath = Paths.get(surnameFile);
      MappingIterator<CensusSurname> it = mapper.reader(CensusSurname.class)
                                                .with(schema)
                                                .readValues(csvPath.toFile());

      List<CensusSurname> records = new ArrayList<>();
      while (it.hasNext()) {
         CensusSurname next = it.next();
         if (next.name.equalsIgnoreCase("ALL OTHER NAMES"))
            continue;

         records.add(next);
      }

      Pattern flt = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
      WeightedObservationHeapSampler<CensusSurname> sampler =
            new WeightedObservationHeapSampler<>(records, (surname) -> {
               return surname.count;
//               String pct = surname.pctapi;
//               return flt.matcher(pct).matches()
//                     ? surname.count * Double.parseDouble(pct)
//                     : 0;
            });

      long start = System.currentTimeMillis();
      List<CensusSurname> samples = sampler.sampleWithReplacement(1000);
      long end = System.currentTimeMillis();
      List<String> names = samples.parallelStream()
            .map((s) -> CensusSurnameGenerator.capitalize(s.name.toLowerCase()))
            .collect(Collectors.toList());

      System.out.println("Sample Creation Time: " + (end - start));
      names.forEach(System.out::println);
   }
}
