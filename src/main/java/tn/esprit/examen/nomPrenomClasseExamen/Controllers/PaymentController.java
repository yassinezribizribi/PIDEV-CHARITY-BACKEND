package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonationSuggestionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.dto.PaiementDTO;
import tn.esprit.examen.nomPrenomClasseExamen.dto.PaymentRequest;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Paiement;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;
import tn.esprit.examen.nomPrenomClasseExamen.services.PaiementServices;
import tn.esprit.examen.nomPrenomClasseExamen.services.StripeServices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paiments")
@CrossOrigin(origins = "http://localhost:4200")

@AllArgsConstructor
public class PaymentController {
    private final StripeServices stripeServices;
    private final PaiementServices paiementService; // Inject PaiementService
    private final JwtUtils jwtUtils;

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @PostMapping("/create-payment-intent/{cagnotteId}")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @PathVariable Long cagnotteId,
            @Valid
            @RequestBody PaymentRequest request,
            @RequestHeader(name = "Authorization", required = false) String authHeader) throws StripeException {

        // Extract token from "Bearer <token>"
        String jwtToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7); // Remove "Bearer " prefix
        }

        // Call service method with token
        PaiementDTO paiementDTO = new PaiementDTO(); // Assuming you will adapt or already have a DTO (or we can inline fields)
        Paiement paiement = paiementService.createPaiement(paiementDTO, request.getAmount(), cagnotteId, jwtToken);

        // Get client secret from Stripe PaymentIntent
        PaymentIntent paymentIntent = stripeServices.createPaymentIntent(request.getAmount(), "eur");

        Map<String, String> responseData = new HashMap<>();
        responseData.put("clientSecret", paymentIntent.getClientSecret());

        return ResponseEntity.ok(responseData);
    }


//    @GetMapping("/cagnotte/{id}")
//    public ResponseEntity<DonationSuggestionDTO> getSuggestions(
//            @PathVariable Long id,
//            @RequestParam("input") Integer montant) {
//
//        DonationSuggestionDTO suggestions = paiementService.getSuggestions(id, montant);
//        return ResponseEntity.ok(suggestions);
//    }



//    @GetMapping("/wheel-suggestions/{cagnotteId}")
//    public ResponseEntity<List<Integer>> getWheelSuggestions(
//            @PathVariable Long cagnotteId,
//            @RequestParam(required = false) Integer amount) {
//        return ResponseEntity.ok(paiementService.getWheelSuggestedAmounts(cagnotteId, amount));
//    }

    // ✅ NEW: Get 7 spin suggestions for a cagnotte
//    @GetMapping("/spin/{cagnotteId}")
//    public ResponseEntity<List<Integer>> getSpinSuggestions(
//            @PathVariable Long cagnotteId,
//            @RequestParam Long userId,
//
//            @RequestParam(value = "userInput", required = false) Integer userInput) {
//
//        List<Integer> suggestions = paiementService.getWheelSuggestedAmounts(cagnotteId, userId,userInput);
//        return ResponseEntity.ok(suggestions);
//    }

    // ✅ NEW: Get 7 spin suggestions for a cagnotte

    @GetMapping("/spin/{cagnotteId}")
    public ResponseEntity<List<Integer>> getWheelSuggestions(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long cagnotteId,
            @RequestParam(value = "userInput", required = false) Integer userInput
    ) {
        String jwtToken = extractJwtToken(authorizationHeader);
        List<Integer> suggestions = paiementService.getWheelSuggestedAmounts(jwtToken, cagnotteId, userInput);
        return suggestions == null || suggestions.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(suggestions);
    }

    private String extractJwtToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    //////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    @GetMapping("/cagnotte/suggestions/{cagnotteId}")
    public ResponseEntity<DonationSuggestionDTO> getSuggestions(
            @PathVariable Long cagnotteId,
            @RequestParam(value = "input", required = false) Integer userInput,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {

        String jwtToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
        }

        DonationSuggestionDTO suggestion =
                paiementService.getSuggestions(jwtToken, cagnotteId, userInput);

        if (suggestion == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(suggestion);
    }


}

