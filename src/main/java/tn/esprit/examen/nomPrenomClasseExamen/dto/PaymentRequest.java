package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private Long amount;      // Amount in cents (e.g., 1000 = 10.00 EUR)
//    private Long cagnotteId; // ID of the Cagnotte associated with the payment
}
