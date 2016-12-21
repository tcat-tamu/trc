package edu.tamu.tcat.trc.entries.types.biblio.impl.repo;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.DateDescriptionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.PublicationInfoDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;

@SuppressWarnings("deprecation")
public class ModelAdapter
{

   public static DataModelV1.PublicationInfoDTO adapt(PublicationInfoDTO legacy)
   {
      DataModelV1.PublicationInfoDTO dto = new DataModelV1.PublicationInfoDTO();

      if (legacy != null)
      {
         dto.publisher = legacy.publisher;
         dto.place = legacy.place;
         dto.date = adapt(legacy.date);
      }

      return dto;
   }

   public static DataModelV1.DateDescriptionDTO adapt(DateDescriptionDTO legacy)
   {
      DataModelV1.DateDescriptionDTO dto = new DataModelV1.DateDescriptionDTO();

      if (legacy != null)
      {
         dto.calendar = legacy.calendar;
         dto.description = legacy.description;
      }

      return dto;
   }

   public static List<DataModelV1.TitleDTO> adaptTitles(Collection<TitleDTO> legacy)
   {
      return legacy.stream().map(ModelAdapter::adapt).collect(toList());
   }

   public static DataModelV1.TitleDTO adapt(TitleDTO legacy)
   {
      DataModelV1.TitleDTO dto = new DataModelV1.TitleDTO();

      if (legacy != null)
      {
         dto.type = legacy.type;
         dto.title = legacy.title;
         dto.subtitle = legacy.subtitle;
         dto.lg = legacy.lg;
      }

      return dto;
   }

   public static List<DataModelV1.AuthorReferenceDTO> adaptAuthors(List<AuthorReferenceDTO> legacy)
   {
      return legacy.stream().map(ModelAdapter::adapt).collect(toList());
   }

   public static DataModelV1.AuthorReferenceDTO adapt(AuthorReferenceDTO legacy)
   {
      DataModelV1.AuthorReferenceDTO dto = new DataModelV1.AuthorReferenceDTO();

      if (legacy != null)
      {
         dto.authorId = legacy.authorId;
         dto.firstName = legacy.firstName;
         dto.lastName = legacy.lastName;
         dto.role = legacy.role;
      }

      return dto;
   }

}
