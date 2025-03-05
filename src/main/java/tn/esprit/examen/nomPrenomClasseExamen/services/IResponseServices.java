package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.ResponseDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Response;

import java.util.List;

public interface IResponseServices {
    List<ResponseDto> getAllResponses();
    ResponseDto getResponseById(Long id);
    Response addResponseToRequest(Long requestId, ResponseDto responseDto);
    ResponseDto updateResponse(Long id, ResponseDto responseDto);
    void deleteResponse(Long id);
    List<ResponseDto> getResponsesByRequestId(Long requestId);
    ResponseDto createResponse(ResponseDto responseDto); // Méthode manquante

}
