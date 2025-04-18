package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

@Service
@Slf4j
public class NominatimService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Double[] getCoordinatesFromAddress(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?q=" + encodedAddress + "&format=json&limit=1";

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JSONArray jsonArray = new JSONArray(response.getBody());

            if (jsonArray.length() > 0) {
                JSONObject location = jsonArray.getJSONObject(0);
                double lat = Double.parseDouble(location.getString("lat"));
                double lon = Double.parseDouble(location.getString("lon"));
                return new Double[]{lat, lon};
            }
        } catch (Exception e) {
            log.error("Erreur lors de la géolocalisation : ", e);
        }
        return null;
    }
}
