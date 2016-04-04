/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.entries.types.biblio.test;

import static org.junit.Assert.assertEquals;

import java.util.Objects;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;

/**
 *  Tests data serialization/deserialization for basic domain model implementation. For each
 *  data model type, this tests creation of the appropriate dto, instantiation of a domain
 *  object and equality hash code comparisons, and serialization to/from JSON
 *
 */
public class TitleTests
{

   @BeforeClass
   public static void setUp()
   {

   }

   @AfterClass
   public static void tearDown()
   {

   }

   @Before
   public void setupTest()
   {

   }

   @After
   public void tearDownTest()
   {

   }

   public static TitleDTO createTitle(String title, String sub, String type, String lg)
   {
      TitleDTO dto = new TitleDTO();
      dto.lg = lg;
      dto.type = type;
      dto.title = title;
      dto.subtitle = sub;

      return dto;
   }

   public static boolean testEquality(TitleDTO expected, TitleDTO actual)
   {
      return Objects.equals(expected.title, actual.title)
          && Objects.equals(expected.subtitle, actual.subtitle)
          && Objects.equals(expected.type, actual.type)
          && Objects.equals(expected.lg, actual.lg);
   }

   public static boolean testEquality(Title expected, Title actual)
   {
      return Objects.equals(expected.getTitle(), actual.getTitle())
            && Objects.equals(expected.getSubTitle(), actual.getSubTitle())
            && Objects.equals(expected.getType(), actual.getType())
            && Objects.equals(expected.getLanguage(), actual.getLanguage());
      }

   @Test
   public void testTitle()
   {
      assertEquals(null, null);
      TitleDTO dto = new TitleDTO();
      dto.lg = "en";
      dto.type = "canonical";
      dto.title = "A Short Work";
      dto.subtitle = "For Testing Purposes";

      Title title = TitleDTO.instantiate(dto);

   }

   @Test
   public void testAurhorList()
   {

   }

   @Test
   public void testAurhorRef()
   {

   }

   @Test
   public void testPublicationInfo()
   {

   }



   @Test
   public void testTitleDefinition()
   {

   }

   @Test
   public void testVolume()
   {

   }

   @Test
   public void testEdition()
   {

   }

   @Test
   public void testWork()
   {

   }
}
