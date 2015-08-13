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

import edu.tamu.tcat.trc.entries.types.bio.test.names.CensusSurnameGenerator.CensusSurname;


public class TestObservationSampler
{
   String surnameFile = "D:\\data\\us-names\\census\\surnames\\2010\\app_c.csv";

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
         records.add(it.next());
      }

      Pattern flt = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
      WeightedObservationHeapSampler<CensusSurname> sampler =
            new WeightedObservationHeapSampler<>(records, (surname) -> {
               String pct = surname.pctapi;
               return flt.matcher(pct).matches()
                     ? surname.count * Double.parseDouble(pct)
                     : 0;
            });

      long start = System.currentTimeMillis();
      List<CensusSurname> samples = sampler.sampleWithReplacement(10_000);
      List<String> names = samples.parallelStream().map((s) -> s.name).collect(Collectors.toList());
      long end = System.currentTimeMillis();

      System.out.println("Sample Creation Time: " + (end - start));
      names.forEach(System.out::println);
   }
}
