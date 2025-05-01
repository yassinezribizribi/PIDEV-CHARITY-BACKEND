package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private Long senderId;
    private Long receiverId;
    private LocalDateTime timestamp;
    @Column(name = "`read`") // Escape with backticks
    private boolean read;
    @ManyToOne
    @JoinColumn(name = "conversation_id")
    @JsonBackReference

    private Conversation conversation;
}
