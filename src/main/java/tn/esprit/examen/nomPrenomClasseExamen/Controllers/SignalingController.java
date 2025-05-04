package tn.esprit.examen.nomPrenomClasseExamen.Controllers;
import jakarta.mail.MessagingException;
import org.modelmapper.spi.ErrorMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import tn.esprit.examen.nomPrenomClasseExamen.dto.*;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.simp.user.SimpUser;

@Controller
@CrossOrigin
public class SignalingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;

    public SignalingController(SimpMessagingTemplate messagingTemplate,
                               SimpUserRegistry userRegistry) {
        this.messagingTemplate = messagingTemplate;
        this.userRegistry = userRegistry;
    }

    @MessageMapping("/signal/offer")
    public void handleOffer(@Payload OfferMessage offer) {
        try {
            validateUserConnection(offer.getSenderId().toString());
            validateUserConnection(offer.getTargetUserId().toString());

            messagingTemplate.convertAndSendToUser(
                    offer.getTargetUserId().toString(),
                    "/queue/signal",
                    offer
            );
        } catch (MessagingException e) {
            // Send error back to sender
            messagingTemplate.convertAndSendToUser(
                    offer.getSenderId().toString(),
                    "/queue/errors",
                    new ErrorMessage(e.getMessage())
            );
        }
    }

    @MessageMapping("/signal/answer")
    public void handleAnswer(@Payload AnswerMessage answer) {
        try {
            validateUserConnection(answer.getSenderId().toString());
            validateUserConnection(answer.getTargetUserId().toString());

            messagingTemplate.convertAndSendToUser(
                    answer.getTargetUserId().toString(),
                    "/queue/signal",
                    answer
            );
        } catch (MessagingException e) {
            messagingTemplate.convertAndSendToUser(
                    answer.getSenderId().toString(),
                    "/queue/errors",
                    new ErrorMessage(e.getMessage())
            );
        }
    }

    @MessageMapping("/signal/ice-candidate")
    public void handleIceCandidate(@Payload IceCandidateMessage candidate) {
        try {
            validateUserConnection(candidate.getSenderId().toString());
            validateUserConnection(candidate.getTargetUserId().toString());

            messagingTemplate.convertAndSendToUser(
                    candidate.getTargetUserId().toString(),
                    "/queue/signal",
                    candidate
            );
        } catch (MessagingException e) {
            messagingTemplate.convertAndSendToUser(
                    candidate.getSenderId().toString(),
                    "/queue/errors",
                    new ErrorMessage(e.getMessage())
            );
        }
    }

    private void validateUserConnection(String userId) throws MessagingException {
        SimpUser user = userRegistry.getUser(userId);
        if (user == null) {
            throw new MessagingException("User " + userId + " not connected");
        }
    }
}