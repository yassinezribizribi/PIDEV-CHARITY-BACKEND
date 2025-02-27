package tn.esprit.examen.nomPrenomClasseExamen.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Response;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Request;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDto {

    private Long idResponse;

    @NotNull(message = "Sender ID cannot be null")
    private Long idSender;

    @NotNull(message = "Receiver ID cannot be null")
    private Long idReceiver;

    private Date dateResponse;

    @NotNull(message = "Content cannot be null")
    private String content;

    private String object;

    private Set<Long> requestIds;

    // Convert Entity to DTO
    public static ResponseDto fromEntity(Response response) {
        return new ResponseDto(
                response.getIdRespons(),
                response.getIdSender(),
                response.getIdReceiver(),
                response.getDateRespons(),
                response.getContent(),
                response.getObject(),
                response.getRequests() != null ?
                        response.getRequests().stream().map(Request::getIdRequest).collect(Collectors.toSet()) : null
        );
    }

    // Convert DTO to Entity
    public static Response toEntity(ResponseDto dto, Set<Request> requests) {
        Response response = new Response();
        response.setIdRespons(dto.getIdResponse());
        response.setIdSender(dto.getIdSender());
        response.setIdReceiver(dto.getIdReceiver());
        response.setDateRespons(dto.getDateResponse());
        response.setContent(dto.getContent());
        response.setObject(dto.getObject());
        response.setRequests(requests);
        return response;
    }
}
