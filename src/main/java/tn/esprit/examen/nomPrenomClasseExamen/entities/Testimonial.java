package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "testimonial")
public class Testimonial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "dominant_emotion")
    private String dominantEmotion;

    private String beforePhotoPath;
    private String afterPhotoPath;
    @Column(name = "media_path")
    private String mediaPath;

    @Column(name = "media_type")
    private String mediaType;

    @Column(name = "transcription_text", columnDefinition = "TEXT")
    private String transcriptionText;
    @Column(columnDefinition = "TEXT")
    private String summary;

    @Transient
    private String suggestedResponse;

    @Transient
    private List<String> recommendedResources;




    public Testimonial(String content, String description, String beforePhotoPath, String afterPhotoPath) {
        this.content = content;
        this.description = description;
        this.beforePhotoPath = beforePhotoPath;
        this.afterPhotoPath = afterPhotoPath;
    }
}
