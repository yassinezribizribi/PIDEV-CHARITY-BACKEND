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
        logger.info("📢 Fetching response with ID: {}", id);
        return responseRepository.findById(id)
                .map(ResponseDto::fromEntity)
                .orElseThrow(() -> new RuntimeException("Response not found with ID: " + id));
    }

    @Override
    public Response addResponseToRequest(Long requestId, ResponseDto responseDto) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        Response response = new Response();
        response.setContent(responseDto.getContent());
        response.setRequest(request);

        return responseRepository.save(response);
    }

    @Override
    public ResponseDto createResponse(ResponseDto responseDto) {
        logger.info("📢 Creating a new response...");

        if (responseDto.getRequestId() == null) {
            throw new RuntimeException("Request ID is required to create a response.");
        }

        Request request = requestRepository.findById(responseDto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found with ID: " + responseDto.getRequestId()));

        Response response = new Response();
        response.setContent(responseDto.getContent());
        response.setRequest(request);

        Response savedResponse = responseRepository.save(response);
        logger.info("✅ Response created successfully: {}", savedResponse);

        return ResponseDto.fromEntity(savedResponse);
    }

    @Override
    public ResponseDto updateResponse(Long id, ResponseDto responseDto) {
        logger.info("🔄 Updating response with ID: {}", id);

        Response existingResponse = responseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Response with ID " + id + " not found"));

        existingResponse.setDateRespons(responseDto.getDateResponse());
        existingResponse.setContent(responseDto.getContent());
        existingResponse.setObject(responseDto.getObject());

        if (responseDto.getRequestId() != null) {
            Request request = requestRepository.findById(responseDto.getRequestId())
                    .orElseThrow(() -> new RuntimeException("Request not found with ID: " + responseDto.getRequestId()));
            existingResponse.setRequest(request);
        }

        Response updatedResponse = responseRepository.save(existingResponse);
        logger.info("✅ Response updated successfully: {}", updatedResponse);
        return ResponseDto.fromEntity(updatedResponse);
    }

    @Override
    public void deleteResponse(Long id) {
        logger.info("🗑 Deleting response with ID: {}", id);
        if (!responseRepository.existsById(id)) {
            throw new RuntimeException("Response with ID " + id + " not found");
        }
        responseRepository.deleteById(id);
        logger.info("✅ Response deleted successfully!");
    }

    @Override
    public List<ResponseDto> getResponsesByRequestId(Long requestId) {
        logger.info("📢 Fetching responses for request ID: {}", requestId);
        return responseRepository.findByRequest_IdRequest(requestId).stream()
                .map(ResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}