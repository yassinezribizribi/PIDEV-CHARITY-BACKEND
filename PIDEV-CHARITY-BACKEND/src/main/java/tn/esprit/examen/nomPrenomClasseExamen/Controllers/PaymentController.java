package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.PaymentRequest;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Paiement;
import tn.esprit.examen.nomPrenomClasseExamen.services.PaiementServices;
import tn.esprit.examen.nomPrenomClasseExamen.services.StripeServices;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/paiments")
@CrossOrigin(origins = "http://localhost:4200")

@AllArgsConstructor
public class PaymentController {
    private final StripeServices stripeServices;
    private final PaiementServices paiementService; // Inject PaiementService

    @PostMapping("/create-payment-intent/{cagnotteId}")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@PathVariable Long cagnotteId, @RequestBody PaymentRequest request) throws StripeException {
        // Create the PaymentIntent using Stripe
        PaymentIntent paymentIntent = stripeServices.createPaymentIntent(request.getAmount(), "eur");

        // Create a new Paiement entity and save it in the database
        Paiement paiement = new Paiement();
        paiement = paiementService.createPaiement(paiement, request.getAmount(), cagnotteId); // Pass cagnotteId from the URL

        // Return the client secret for frontend to use
        Map<String, String> responseData = new HashMap<>();
        responseData.put("clientSecret", paymentIntent.getClientSecret()); // Ensure we're sending Stripe's clientSecret

        return ResponseEntity.ok(responseData);
    }




}
