package tn.esprit.examen.nomPrenomClasseExamen.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ScheduledDateValidator.class)
@Documented
public @interface ValidScheduledDate {
    String message() default "Scheduled date must not be after the campaign end date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
