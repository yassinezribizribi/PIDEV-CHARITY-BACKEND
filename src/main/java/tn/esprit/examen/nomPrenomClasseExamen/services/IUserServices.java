package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.entities.User;
import java.util.Optional;

public interface IUserServices {
    Optional<User> findByUsername(String username);
}
