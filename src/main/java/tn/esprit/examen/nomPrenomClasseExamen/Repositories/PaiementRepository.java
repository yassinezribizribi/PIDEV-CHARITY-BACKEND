package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Paiement;

import java.util.List;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    // 📊 Campaign average for a specific cagnotte
    // 📊 Average donation for a given campaign
    @Query("SELECT AVG(p.montant) FROM Paiement p WHERE p.cagnotte.idCagnotte = :cagnotteId")
    Double findAverageByCagnotteId(@Param("cagnotteId") Long cagnotteId);

    // 📈 Today's average donation for a campaign
    @Query("SELECT AVG(p.montant) FROM Paiement p WHERE p.cagnotte.idCagnotte = :cagnotteId AND FUNCTION('DATE', p.datePaiement) = CURRENT_DATE")
    Double findTodayAverageByCagnotteId(@Param("cagnotteId") Long cagnotteId);

    @Query("SELECT AVG(p.montant) FROM Paiement p WHERE p.utilisateur.idUser = :userId")
    Double findAverageByUserId(@Param("userId") Long userId);
    Paiement findTopByUtilisateur_IdUserAndCagnotte_IdCagnotteOrderByDatePaiementDesc(Long userId, Long cagnotteId);

    // In PaiementRepository
    @Query("SELECT MAX(p.montant) FROM Paiement p WHERE p.utilisateur.idUser = :userId")
    Integer findMaxDonationByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(p.montant) FROM Paiement p WHERE p.utilisateur.idUser = :userId")
    Double findAverageDonationByUserId(@Param("userId") Long userId);

}