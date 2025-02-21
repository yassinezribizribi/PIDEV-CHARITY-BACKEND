package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.RequestRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.ForumRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.RequestDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Request;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Forum;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RequestService {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ForumRepository forumRepository;

    public Request addRequest(RequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Request data is null.");
        }

        Request request = new Request();
        request.setIdSender(requestDTO.getIdSender());
        request.setIdReceiver(requestDTO.getIdReceiver());
        request.setDateRequest(requestDTO.getDateRequest());
        request.setObject(requestDTO.getObject());
        request.setContent(requestDTO.getContent());
        request.setIsUrgent(requestDTO.getIsUrgent());

        // Linking to Forum
        Forum forum = forumRepository.findById(requestDTO.getForumId())
                .orElseThrow(() -> new IllegalArgumentException("Forum not found with ID: " + requestDTO.getForumId()));
        request.setForum(forum);

        return requestRepository.save(request);
    }

    public Request updateRequest(Long id, Request updatedRequest) {
        return requestRepository.findById(id)
                .map(existingRequest -> {
                    existingRequest.setObject(updatedRequest.getObject());
                    existingRequest.setContent(updatedRequest.getContent());
                    existingRequest.setIsUrgent(updatedRequest.getIsUrgent());
                    return requestRepository.save(existingRequest);
                }).orElseThrow(() -> new RuntimeException("Request not found with ID: " + id));
    }

    public void deleteRequest(Long id) {
        if (!requestRepository.existsById(id)) {
            throw new RuntimeException("Request not found with ID: " + id);
        }
        requestRepository.deleteById(id);
    }

    public Optional<Request> getRequestById(Long id) {
        return requestRepository.findById(id);
    }

    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }
}
