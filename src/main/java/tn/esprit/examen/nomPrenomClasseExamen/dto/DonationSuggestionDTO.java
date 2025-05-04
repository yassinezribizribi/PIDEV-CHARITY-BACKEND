package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationSuggestionDTO {
    private Long average;          // campaign-wide average donation
    private Long todayAverage;     // today’s average donation
    private Integer suggestion;    // the amount you’re suggesting
    private String message;        // a hint/UX message for the front-end
}
