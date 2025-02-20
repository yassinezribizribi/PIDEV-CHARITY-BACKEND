package tn.esprit.examen.nomPrenomClasseExamen.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Role;

import java.util.Collection;
import java.util.Collections;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class SubDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role; // Utiliser l'Enum Role
    private String telephone;
    private String job;

    @JsonIgnore
    private String password;

    public static SubDetailsImpl build(Subscriber subscriber) {
        return new SubDetailsImpl(
                subscriber.getIdUser(),
                subscriber.getEmail(),
                subscriber.getFirstName(),
                subscriber.getLastName(),
                subscriber.getRole(), // Correction ici
                subscriber.getTelephone(),
                subscriber.getJob(),
                subscriber.getPassword()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.name()) // Correction ici
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}