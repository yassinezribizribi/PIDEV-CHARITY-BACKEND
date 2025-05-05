package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Dons;

import java.util.List;

public interface DonsRepository extends JpaRepository<Dons, Long> {
    List<Dons> findByDonation_IdDonation(Long donationId);
    List<Dons> findBySubscriberDons_IdUser(Long userId);
    Page<Dons> findByDonation_IdDonation(Long idDonation, Pageable pageable);

    Page<Dons> findBySubscriberDons_IdUser(Long userId, Pageable pageable);

}
