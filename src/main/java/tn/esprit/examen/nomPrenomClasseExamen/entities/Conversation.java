package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@Table(
        name = "conversation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"participant1_id", "participant2_id"})
)

public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long participant1Id;
    private Long participant2Id;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonManagedReference

    private List<Message> messages = new ArrayList<>();

    // Helper method to get last message
    public Message getLastMessage() {
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }
}
