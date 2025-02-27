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
                .orElseThrow(() -> new RuntimeException("Response with ID " + id + " not found"));
        return ResponseDto.fromEntity(response);
    }

    @Override
    public ResponseDto createResponse(ResponseDto responseDto) {
        logger.info("📝 Creating new response: {}", responseDto);
        Set<Request> requests = responseDto.getRequestIds() != null ?
                requestRepository.findAllById(responseDto.getRequestIds()).stream().collect(Collectors.toSet()) : null;

        Response response = ResponseDto.toEntity(responseDto, requests);
        Response savedResponse = responseRepository.save(response);

        logger.info("✅ Response created successfully: {}", savedResponse);
        return ResponseDto.fromEntity(savedResponse);
    }

    @Override
    public ResponseDto updateResponse(Long id, ResponseDto responseDto) {
        logger.info("🔄 Updating response with ID: {}", id);
        Response existingResponse = responseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Response with ID " + id + " not found"));

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
        responseRepository.deleteById(id);
        logger.info("✅ Response deleted successfully!");
    }
}

