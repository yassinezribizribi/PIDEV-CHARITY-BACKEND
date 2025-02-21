package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.Data;
import java.util.Date;

@Data
public class RequestDTO {
    private Long idSender;
    private Long idReceiver;
    private Date dateRequest;
    private String object;
    private String content;
    private Boolean isUrgent;
    private Long forumId; // To link the request to a forum
}
