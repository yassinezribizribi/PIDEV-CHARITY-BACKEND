package tn.esprit.examen.nomPrenomClasseExamen.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonationDto;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonsDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.DonsDeliveryMethod;

public class DonorAddressValidator implements ConstraintValidator<ValidDonorAddress, DonsDTO> {

    @Override
    public boolean isValid(DonsDTO dto, ConstraintValidatorContext context) {
        if (dto.getDeliveryMethod() == DonsDeliveryMethod.PICK_UP) {
            boolean isValid = dto.getDonorAddress() != null && !dto.getDonorAddress().trim().isEmpty();

            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Donor address is required for PICK_UP")
                        .addPropertyNode("donorAddress")
                        .addConstraintViolation();

            }

            return isValid;
        }
        return true; // valid for other delivery methods
    }
}
