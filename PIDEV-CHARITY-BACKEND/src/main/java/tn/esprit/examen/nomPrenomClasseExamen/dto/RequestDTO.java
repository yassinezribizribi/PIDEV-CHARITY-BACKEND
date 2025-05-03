package tn.esprit.examen.nomPrenomClasseExamen.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Forum;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Request;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {



    @NotNull(message = "Request date cannot be null")
    private Date dateRequest;

    @NotNull(message = "Object cannot be null")
    private String object;

    @NotNull(message = "Content cannot be null")
    private String content;

    private Boolean isUrgent;

    @NotNull(message = "Forum ID cannot be null")
    @Positive(message = "Forum ID must be a positive number")
    private Long forumId;

    // Static method to convert from entity to DTO
    public static RequestDTO fromEntity(Request request) {
        return new RequestDTO(

                request.getDateRequest(),
                request.getObject(),
                request.getContent(),
                request.getIsUrgent(),
                request.getForum() != null ? request.getForum().getIdForum() : null
        );
    }

    // Static method to convert from DTO to entity
    public static Request toEntity(RequestDTO dto, Forum forum) {
        return new Request(

                dto.getDateRequest(),
                dto.getObject(),
                dto.getContent(),
                dto.getIsUrgent(),
                forum
        );
    }
}