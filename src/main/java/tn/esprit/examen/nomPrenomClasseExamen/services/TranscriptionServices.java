package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class TranscriptionServices {

    private final String OPENAI_API_KEY = "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";

    public String transcribeMedia(Path mediaPath) throws IOException {
        String url = "https://api.openai.com/v1/audio/transcriptions";

        byte[] fileContent = Files.readAllBytes(mediaPath);

        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("model", "whisper-1");

        String transcription = restTemplate.postForObject(builder.toUriString(), fileContent, String.class);

        if (transcription == null) throw new RuntimeException("Erreur lors de la transcription");

        return transcription;
    }
}
