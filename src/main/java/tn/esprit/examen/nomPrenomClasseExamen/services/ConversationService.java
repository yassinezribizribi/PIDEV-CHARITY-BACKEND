package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.ConversationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.MessageRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Conversation;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }


    @Transactional
    public Conversation getOrCreateConversation(Long userId1, Long userId2) {
        Long p1 = Math.min(userId1, userId2);
        Long p2 = Math.max(userId1, userId2);

        // First try exact match
        List<Conversation> existing = conversationRepository
                .findByParticipant1IdAndParticipant2Id(p1, p2);

        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        // If not found, check reverse participants
        List<Conversation> reverse = conversationRepository
                .findByParticipant1IdAndParticipant2Id(p2, p1);

        if (!reverse.isEmpty()) {
            return reverse.get(0);
        }

        // Only create new if truly doesn't exist
        Conversation newConv = new Conversation();
        newConv.setParticipant1Id(p1);
        newConv.setParticipant2Id(p2);
        return conversationRepository.save(newConv);
    }




    private Conversation createConversation(Long p1, Long p2) {
        Conversation conv = new Conversation();
        conv.setParticipant1Id(Math.min(p1, p2));
        conv.setParticipant2Id(Math.max(p1, p2));
        return conversationRepository.save(conv);
    }

    public List<Conversation> getUserConversations(Long userId) {
        return conversationRepository.findByParticipant1IdOrParticipant2Id(userId, userId);
    }
    public List<Message> getUnreadMessages(Long userId) {
        return messageRepository.findByReceiverIdAndReadFalse(userId);
    }

    public void markSingleMessageAsRead(Long messageId) {
        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        msg.setRead(true);
        messageRepository.save(msg);
    }

    @Transactional
    public List<Message> getConversationMessages(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Force loading messages
        conversation.getMessages().size();

        return conversation.getMessages();
    }


    public Message sendMessage(Long conversationId, Long senderId, String content) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Message message = new Message();
        message.setContent(content);
        message.setSenderId(senderId);
        message.setReceiverId(getReceiverId(conv, senderId));
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        message.setConversation(conv);

        return messageRepository.save(message);
    }

    private Long getReceiverId(Conversation conv, Long senderId) {
        return conv.getParticipant1Id().equals(senderId) ?
                conv.getParticipant2Id() : conv.getParticipant1Id();
    }

    public void markMessagesAsRead(Long conversationId, Long userId) {
        List<Message> messages = messageRepository
                .findByConversationIdAndReceiverIdAndReadFalse(conversationId, userId);

        messages.forEach(msg -> msg.setRead(true));
        messageRepository.saveAll(messages);
    }
}