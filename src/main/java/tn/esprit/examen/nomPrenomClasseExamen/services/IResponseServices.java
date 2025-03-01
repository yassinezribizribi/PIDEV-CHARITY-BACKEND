package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.ResponseDto;

import java.util.List;

public interface IResponseServices {
    List<ResponseDto> getAllResponses();
    ResponseDto getResponseById(Long id);
    ResponseDto createResponse(ResponseDto responseDto); // Correction du nom de la méthode
    ResponseDto updateResponse(Long id, ResponseDto responseDto);
    void deleteResponse(Long id);
}
