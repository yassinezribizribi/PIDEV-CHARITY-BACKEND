package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
@JsonIgnoreProperties({"likedByUsers", "subscription"})
public class Posts implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPosts;

    private String content;
    private Date creationDate;

    @Column(name = "image_path")
    private String imagePath;
    @OneToMany(cascade = CascadeType.ALL, mappedBy="posts")
    @JsonIgnore
    private Set<PostAction> postActions;

    private Long likesCount = Long.valueOf(0);

    @OneToMany(cascade = CascadeType.ALL, mappedBy="post", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("post") // pour éviter la récursion infinie
    private List<Comment> Comments;

    @ManyToMany
    @JsonIgnore
    @JoinTable(
            name = "post_likes",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedByUsers = new HashSet<>();

    @Transient // This field is NOT persisted in the database
    private boolean likedByUser;

    // Getter and Setter for likedByUser
    public boolean isLikedByUser() {
        return likedByUser;
    }

    public void setLikedByUser(boolean likedByUser) {
        this.likedByUser = likedByUser;
    }

    @ManyToOne(optional = true)
    @JsonIgnore
    Subscription subscription;

}
