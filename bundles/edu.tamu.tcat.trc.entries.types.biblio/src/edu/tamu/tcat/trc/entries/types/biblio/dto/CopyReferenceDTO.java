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
package edu.tamu.tcat.trc.entries.types.biblio.dto;

import java.util.HashMap;
import java.util.Map;

import edu.tamu.tcat.trc.entries.types.biblio.CopyReference;

public class CopyReferenceDTO
{
   public String id;
   public String type;
   public Map<String, String> properties = new HashMap<>();
   public String title;
   public String summary;
   public String rights;

   public CopyReferenceDTO()
   {

   }

   /** Copy constructor */
   public CopyReferenceDTO(CopyReferenceDTO orig)
   {
      this.id = orig.id;
      this.type = orig.type;
      this.properties = new HashMap<>(orig.properties);
      this.title = orig.title;
      this.summary = orig.summary;
      this.rights = orig.rights;
   }

   public static CopyReferenceDTO create(CopyReference ref)
   {
      CopyReferenceDTO dto = new CopyReferenceDTO();

      dto.id = ref.getId();
      dto.type = ref.getType();
      dto.properties = ref.getProperties();

      dto.title = ref.getTitle();
      dto.summary = ref.getSummary();
      dto.rights = ref.getRights();

      return dto;
   }

   public static CopyReferenceDTO copy(CopyReferenceDTO orig)
   {
      CopyReferenceDTO dto = new CopyReferenceDTO();

      dto.id = orig.id;
      dto.type = orig.type;

      if (orig.properties != null) {
         dto.properties = new HashMap<>(orig.properties);
      }

      dto.title = orig.title;
      dto.summary = orig.summary;
      dto.rights = orig.rights;

      return dto;
   }
}
