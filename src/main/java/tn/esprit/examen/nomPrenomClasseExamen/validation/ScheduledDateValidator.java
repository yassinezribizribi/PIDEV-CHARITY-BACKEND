package tn.esprit.examen.nomPrenomClasseExamen.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.DonationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonsDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Donation;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;



@Component
@RequiredArgsConstructor
public class ScheduledDateValidator implements ConstraintValidator<ValidScheduledDate, DonsDTO> {

    private final DonationRepository donationRepository;

    @Override
    public boolean isValid(DonsDTO dto, ConstraintValidatorContext context) {
        System.out.println("✅ ScheduledDateValidator triggered for DTO: " + dto);

        if (dto.getScheduledDateTime() == null || dto.getDonationId() == null) {
            return true;
        }

        Donation donation = donationRepository.findById(dto.getDonationId()).orElse(null);
        if (donation == null || donation.getEndDate() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Donation not found or has no end date")
                    .addPropertyNode("donationId")
                    .addConstraintViolation();
            return false;
        }

        LocalDateTime endDateTime = donation.getEndDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .atTime(23, 59, 59); // end of the donation day

        if (dto.getScheduledDateTime().isAfter(endDateTime)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Scheduled date must be on or before the donation end date")
                    .addPropertyNode("scheduledDateTime")
                    .addConstraintViolation();

            System.out.println("❌ Scheduled date is after end date");
            return false;
        }

        // 🚫 Block weekends
        DayOfWeek scheduledDay = dto.getScheduledDateTime().getDayOfWeek();
        if (scheduledDay == DayOfWeek.SATURDAY || scheduledDay == DayOfWeek.SUNDAY) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Scheduled donations cannot be on weekends.")
                    .addPropertyNode("scheduledDateTime")
                    .addConstraintViolation();

            System.out.println("❌ Scheduled date is on a weekend");
            return false;
        }

        return true;
    }
}
