package tn.esprit.examen.nomPrenomClasseExamen.services;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.CagnotteEnligneRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.PaiementRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.CagnotteEnligne;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Paiement;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class PaiementServices {

    private final PaiementRepository paiementRepository;
    private final StripeServices stripeServices;
    private final CagnotteEnligneRepository cagnotteEnligneRepository;

    // Create the payment, update the cagnotte, and save both to the database
    public Paiement createPaiement(Paiement paiement, Long amount, Long cagnotteId) throws StripeException {
        // Create PaymentIntent with Stripe
        PaymentIntent paymentIntent = stripeServices.createPaymentIntent(amount, "eur");

        // Set the payment details
        paiement.setDatePaiement(new Date()); // Set the current date for payment
        paiement.setMontant(amount.intValue()); // Set the amount (in cents, so make sure it's correct)
        paiement.setStripePaymentId(paymentIntent.getId()); // Stripe payment ID
        paiement.setStatus(paymentIntent.getStatus()); // Payment status (succeeded, pending, etc.)
        paiement.setCurrency(paymentIntent.getCurrency()); // Currency (e.g., eur, usd)

        // Fetch the cagnotte using the cagnotteId and set it in the Paiement entity
        CagnotteEnligne cagnotte = cagnotteEnligneRepository.findById(cagnotteId)
                .orElseThrow(() -> new RuntimeException("Cagnotte not found"));
        paiement.setCagnotte(cagnotte); // Set the cagnotte relationship

        // Save the payment details in the database
        Paiement savedPaiement = paiementRepository.save(paiement);

        // Now, update the associated cagnotte's currentAmount
        cagnotte.setCurrentAmount(cagnotte.getCurrentAmount() + amount); // Increment the cagnotte's currentAmount with the payment amount
        cagnotteEnligneRepository.save(cagnotte); // Save the updated cagnotte

        return savedPaiement;
    }


    // Process Stripe Payment without creating Paiement, if needed
    public PaymentIntent processStripePayment(Long amount) throws StripeException {
        return stripeServices.createPaymentIntent(amount, "usd"); // or "eur" / "tnd"
    }
}