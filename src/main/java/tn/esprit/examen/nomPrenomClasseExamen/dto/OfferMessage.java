package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfferMessage {
    private String senderId;
    private String targetUserId;
    private Object offer;
}
