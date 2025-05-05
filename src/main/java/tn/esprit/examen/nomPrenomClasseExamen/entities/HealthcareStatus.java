package tn.esprit.examen.nomPrenomClasseExamen.entities;

public enum HealthcareStatus {
    PENDING,       // En attente de validation par le médecin
    IN_PROGRESS,   // Traitement en cours
    COMPLETED,     // Traitement terminé
    ACCEPTED,      // Accepté (actuellement utilisé ?)

    PROPOSED,      // 🟡 Le patient a proposé une date
    CONFIRMED      // 🟢 Le médecin a confirmé cette date
}

