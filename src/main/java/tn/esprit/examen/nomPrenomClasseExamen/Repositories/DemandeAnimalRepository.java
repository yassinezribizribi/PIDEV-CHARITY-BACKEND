package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Animal;
import tn.esprit.examen.nomPrenomClasseExamen.entities.DemandeAnimal;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;

import java.util.List;
import java.util.Optional;

public interface DemandeAnimalRepository extends JpaRepository<DemandeAnimal, Long> {
    Optional<DemandeAnimal> findByUserAndAnimal(User user, Animal animal);
    @Query("SELECT d FROM DemandeAnimal d WHERE d.user = :user AND d.accepted = true")
    List<DemandeAnimal> findAcceptedDemandesByUser(@Param("user") User user);
}
