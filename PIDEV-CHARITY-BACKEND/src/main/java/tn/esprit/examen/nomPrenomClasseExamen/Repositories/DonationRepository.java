package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Donation;
import tn.esprit.examen.nomPrenomClasseExamen.entities.DonationType;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByDonationType(DonationType donationType);
    List<Donation> findByAssociationDonationIdAssociation(Long associationId);
    @Query("SELECT d FROM Donation d LEFT JOIN FETCH d.cagnotteenligne")
    List<Donation> findAllWithCagnottes();
}
