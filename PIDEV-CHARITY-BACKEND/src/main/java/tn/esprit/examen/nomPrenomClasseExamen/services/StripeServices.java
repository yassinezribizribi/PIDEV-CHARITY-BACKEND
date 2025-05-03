package tn.esprit.examen.nomPrenomClasseExamen.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripeServices {

    @Value("${stripe.api.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public PaymentIntent createPaymentIntent(Long amount, String currency) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount); // 💡 in *cents* (e.g., 1000 = 10.00 TND)
        params.put("currency", currency);
        params.put("payment_method_types", List.of("card"));

        return PaymentIntent.create(params);
    }
}
