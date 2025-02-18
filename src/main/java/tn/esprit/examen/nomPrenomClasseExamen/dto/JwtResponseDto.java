package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtResponseDto {
    private String token;
    private String type = "Bearer";
    private Long idUser;
    private List<String> roles;
    public JwtResponseDto(String token, Long idUser, List<String> roles) {
        this.token = token;
        this.idUser = idUser;
        this.roles = roles;
    }


}


