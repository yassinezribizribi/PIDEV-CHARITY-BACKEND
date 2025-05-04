package tn.esprit.examen.nomPrenomClasseExamen.services;


import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GeoCodingService {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    public GeoLocation getGeoLocationFromAddress(String address) {
        String url = UriComponentsBuilder.fromHttpUrl(NOMINATIM_URL)
                .queryParam("q", address)
                .queryParam("format", "json")
                .queryParam("addressdetails", 1)
                .build().toString();

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<GeoLocation[]> response = restTemplate.getForEntity(url, GeoLocation[].class);

        if (response.getBody() != null && response.getBody().length > 0) {
            GeoLocation location = response.getBody()[0];
            return location;
        } else {
            throw new RuntimeException("Address not found or could not be geocoded");
        }
    }

    public static class GeoLocation {
        private Double lat;
        private Double lon;

        // Getters and setters
        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLon() {
            return lon;
        }

        public void setLon(Double lon) {
            this.lon = lon;
        }
    }
}
