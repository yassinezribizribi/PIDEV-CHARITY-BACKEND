package tn.esprit.examen.nomPrenomClasseExamen.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.TYPE }) // apply at class level
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DonorAddressValidator.class)
@Documented
public @interface ValidDonorAddress {
    String message() default "Donor address is required when delivery method is PICK_UP";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}