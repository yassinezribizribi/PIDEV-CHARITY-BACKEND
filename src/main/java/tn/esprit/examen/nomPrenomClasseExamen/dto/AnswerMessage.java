package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public  class AnswerMessage {
    private String senderId;
    private String targetUserId;
    private Object answer;
    // getters and setters
}