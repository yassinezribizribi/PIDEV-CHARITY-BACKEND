package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.ForumRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.RequestRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.RequestDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Forum;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Request;

import java.util.List;

@Service
@AllArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final ForumRepository forumRepository;
    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);

    public List<Request> getAllRequests() {
        logger.info("📢 Récupération de toutes les demandes...");
        return requestRepository.findAll();
    }

    public Request getRequestById(Long id) {
        logger.info("🔍 Recherche de la demande avec l'ID: {}", id);
        return requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request avec l'ID " + id + " introuvable"));
    }

    public Request createRequest(RequestDTO requestDTO) {
        try {
            logger.info("📝 Création d'une nouvelle demande: {}", requestDTO);

            // Vérification et sauvegarde du Forum si nécessaire
            if (requestDTO.getForumId() != null) {
                Forum forum = forumRepository.findById(requestDTO.getForumId())
                        .orElseThrow(() -> new RuntimeException("Forum avec l'ID " + requestDTO.getForumId() + " introuvable"));
                requestDTO.setForumId(forum.getIdForum());
            }

            // Conversion DTO → Entité
            Request request = RequestDTO.toEntity(requestDTO, forumRepository.findById(requestDTO.getForumId()).orElse(null));
            Request savedRequest = requestRepository.save(request);

            logger.info("✅ Demande créée avec succès: {}", savedRequest);
            return savedRequest;
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la création de la demande: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la création de la demande: " + e.getMessage());
        }
    }

    public Request updateRequest(Long id, RequestDTO requestDTO) {
        logger.info("🔄 Mise à jour de la demande avec ID: {}", id);
        Request request = getRequestById(id);

        // Mise à jour des champs à partir du DTO
        request.setIdSender(requestDTO.getIdSender());
        request.setIdReceiver(requestDTO.getIdReceiver());
        request.setDateRequest(requestDTO.getDateRequest());
        request.setObject(requestDTO.getObject());
        request.setContent(requestDTO.getContent());
        request.setIsUrgent(requestDTO.getIsUrgent());

        // Vérification et mise à jour du forum si nécessaire
        if (requestDTO.getForumId() != null) {
            Forum forum = forumRepository.findById(requestDTO.getForumId())
                    .orElseThrow(() -> new RuntimeException("Forum avec l'ID " + requestDTO.getForumId() + " introuvable"));
            request.setForum(forum);
        }

        Request updatedRequest = requestRepository.save(request);
        logger.info("✅ Demande mise à jour avec succès: {}", updatedRequest);
        return updatedRequest;
    }

    public void deleteRequest(Long id) {
        logger.info("🗑 Suppression de la demande avec ID: {}", id);
        requestRepository.deleteById(id);
        logger.info("✅ Demande supprimée avec succès !");
    }
}
