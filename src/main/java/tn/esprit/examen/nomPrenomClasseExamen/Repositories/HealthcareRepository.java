package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Healthcare;

import java.util.Date;
import java.util.List;

@Repository
public interface HealthcareRepository extends JpaRepository<Healthcare, Long> {

    // ✅ JPQL Query pour récupérer les soins avec filtres dynamiques
    @Query("SELECT h FROM Healthcare h " +
            "WHERE (:patientName IS NULL OR LOWER(h.subscriber.firstName) LIKE LOWER(CONCAT('%', :patientName, '%')) " +
            "       OR LOWER(h.subscriber.lastName) LIKE LOWER(CONCAT('%', :patientName, '%'))) " +
            "AND (:doctorName IS NULL OR LOWER(h.doctor.firstName) LIKE LOWER(CONCAT('%', :doctorName, '%')) " +
            "       OR LOWER(h.doctor.lastName) LIKE LOWER(CONCAT('%', :doctorName, '%'))) " +
            "AND (:status IS NULL OR h.status = :status) " +
            "AND (:bookingDate IS NULL OR h.bookingDate = :bookingDate)")
    List<Healthcare> searchHealthcare(
            @Param("patientName") String patientName,
            @Param("doctorName") String doctorName,
            @Param("status") String status,
            @Param("bookingDate") Date bookingDate);
}
