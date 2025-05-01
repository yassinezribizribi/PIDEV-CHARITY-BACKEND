package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public  class IceCandidateMessage {
    private String senderId;
    private String targetUserId;
    private Object candidate;
    // getters and setters
}