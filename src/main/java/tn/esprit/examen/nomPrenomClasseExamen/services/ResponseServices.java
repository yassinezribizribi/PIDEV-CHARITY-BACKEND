package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.RequestRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.ResponseRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.ResponseDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Request;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Response;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ResponseServices implements IResponseServices {

    private final ResponseRepository responseRepository;
    private final RequestRepository requestRepository;
    private static final Logger logger = LoggerFactory.getLogger(ResponseServices.class);

    @Override
    public List<ResponseDto> getAllResponses() {
        logger.info("📢 Fetching all responses...");
        return responseRepository.findAll().stream()
                .map(ResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseDto getResponseById(Long id) {
        logger.info("🔍 Fetching response with ID: {}", id);
        Response response = responseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("❌ Response with ID {} not found", id);
                    return new RuntimeException("Response with ID " + id + " not found");
                });
        return ResponseDto.fromEntity(response);
    }

    @Override
    public ResponseDto createResponse(ResponseDto responseDto) {
        logger.info("📝 Creating response: {}", responseDto);

        // Récupérer les requêtes associées
        Set<Request> requests = responseDto.getRequestIds() != null ?
                responseDto.getRequestIds().stream()
                        .map(id -> requestRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Request not found with ID: " + id)))
                        .collect(Collectors.toSet())
                : Set.of();

        // Convertir DTO en entité et associer les requêtes récupérées
        Response response = ResponseDto.toEntity(responseDto, requests);

        // Sauvegarde de la réponse
        Response savedResponse = responseRepository.save(response);
        logger.info("✅ Response created successfully with ID: {}", savedResponse.getIdRespons());

        return ResponseDto.fromEntity(savedResponse);
    }

    @Override
    public ResponseDto updateResponse(Long id, ResponseDto responseDto) {
        logger.info("🔄 Updating response with ID: {}", id);
        Response existingResponse = responseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("❌ Response with ID {} not found", id);
                    return new RuntimeException("Response with ID " + id + " not found");
                });

        existingResponse.setIdSender(responseDto.getIdSender());
        existingResponse.setIdReceiver(responseDto.getIdReceiver());
        existingResponse.setDateRespons(responseDto.getDateResponse());
        existingResponse.setContent(responseDto.getContent());
        existingResponse.setObject(responseDto.getObject());

        if (responseDto.getRequestIds() != null) {
            Set<Request> requests = requestRepository.findAllById(responseDto.getRequestIds()).stream().collect(Collectors.toSet());
            existingResponse.setRequests(requests);
        }

        Response updatedResponse = responseRepository.save(existingResponse);
        logger.info("✅ Response updated successfully: {}", updatedResponse);
        return ResponseDto.fromEntity(updatedResponse);
    }

    @Override
    public void deleteResponse(Long id) {
        logger.info("🗑 Deleting response with ID: {}", id);
        if (!responseRepository.existsById(id)) {
            logger.error("❌ Response with ID {} not found", id);
            throw new RuntimeException("Response with ID " + id + " not found");
        }
        responseRepository.deleteById(id);
        logger.info("✅ Response deleted successfully!");
    }
}
