package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Conversation;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByParticipant1IdAndParticipant2Id(Long p1, Long p2);
    List<Conversation> findByParticipant1IdOrParticipant2Id(Long userId1, Long userId2);
}

