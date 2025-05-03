package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Dons;

import java.util.List;

public interface DonsRepository extends JpaRepository<Dons, Long> {
    List<Dons> findByDonation_IdDonation(Long donationId);

}
