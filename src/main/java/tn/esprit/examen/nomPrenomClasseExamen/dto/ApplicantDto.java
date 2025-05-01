package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.Getter;

@Getter
public class ApplicantDto {
    private final Long idUser;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String telephone;

    public ApplicantDto(Long idUser, String firstName, String lastName, String email, String telephone) {
        this.idUser = idUser;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.telephone = telephone;
    }
}