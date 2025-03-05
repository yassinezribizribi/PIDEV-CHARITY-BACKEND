package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.RequestDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Request;

import java.util.List;

public interface IRequestServices {

    List<Request> getAllRequests();

    Request getRequestById(Long id);

    Request createRequest(RequestDTO requestDTO);

    Request updateRequest(Long id, RequestDTO requestDTO);

    void deleteRequest(Long id);
}
