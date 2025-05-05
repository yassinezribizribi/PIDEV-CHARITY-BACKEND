package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationExtensionSuggestionDTO {
    private long suggestedDays;
    private String message;
    private double donationProgress;
    private double cagnotteProgress;
    private double donationDailyAvg;
    private double cagnotteDailyAvg;
    private int donationRemaining;
    private float cagnotteRemaining;
    private long donationEstimatedDays;
    private long cagnotteEstimatedDays;

}
