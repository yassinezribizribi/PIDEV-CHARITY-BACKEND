package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Role;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    @Query("SELECT s FROM Subscriber s WHERE s.role = :role")
    List<Subscriber> findByRole(@Param("role") Role role);

    @Query("SELECT s FROM Subscriber s WHERE s.email = :email AND s.email IS NOT NULL")
    Optional<Subscriber> findByEmail(@Param("email") @NotNull String email);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM Subscriber s WHERE s.email = :email AND s.email IS NOT NULL")
    Boolean existsByEmail(@Param("email")  String email);

    @Query("SELECT s FROM Subscriber s WHERE s.idUser != :idUser")
    List<Subscriber> findAllExceptUser(@Param("idUser")  Long idUser);
}
