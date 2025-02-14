package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
    //    Optional<User> findByEmail(String email);
}