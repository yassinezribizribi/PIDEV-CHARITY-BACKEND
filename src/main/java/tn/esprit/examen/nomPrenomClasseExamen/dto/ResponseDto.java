package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.Getter;
import lombok.Setter;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Request;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Response;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class ResponseDto {
    private Long id;
    private String content;
    private String object;
    private LocalDateTime dateResponse;
    private Long requestId; // Un seul ID pour une requête associée

    // Convertir une entité Response en DTO
    public static ResponseDto fromEntity(Response response) {
        ResponseDto dto = new ResponseDto();
        dto.setId(response.getIdRespons());
        dto.setContent(response.getContent());
        dto.setObject(response.getObject());
        dto.setDateResponse(response.getDateRespons());
        dto.setRequestId(response.getRequest() != null ? response.getRequest().getIdRequest() : null);
        return dto;
    }

    // Convertir un DTO en entité Response
    public static Response toEntity(ResponseDto dto, Request request) {
        Response response = new Response();
        response.setIdRespons(dto.getId());
        response.setContent(dto.getContent());
        response.setObject(dto.getObject());
        response.setDateRespons(dto.getDateResponse());
        response.setRequest(request);
        return response;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}