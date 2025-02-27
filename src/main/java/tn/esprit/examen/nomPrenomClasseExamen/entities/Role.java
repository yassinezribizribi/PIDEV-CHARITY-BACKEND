package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    ASSOCIATION_MEMBER,
    VOLUNTEER,
    REFUGEE,
    MENTOR;

    @JsonCreator
    public static Role fromString(String value) {
        try {
            return Role.valueOf(value.toUpperCase());  // Convertit l'entrée en majuscule et la valide
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role specified: " + value);
        }
    }
}
