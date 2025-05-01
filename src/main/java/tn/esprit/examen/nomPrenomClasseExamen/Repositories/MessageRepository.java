package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Message;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByTimestampAsc(Long conversationId);
    // New method for marking messages as read
    List<Message> findByConversationIdAndReceiverIdAndReadFalse(
            Long conversationId,
            Long receiverId
    );

    List<Message> findByReceiverIdAndReadFalse(Long receiverId);

}