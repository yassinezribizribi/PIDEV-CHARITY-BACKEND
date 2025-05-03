package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the response data structure for Rest API responses.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponsee {
  private int statusCode;
  private String status;
  private String reason;
  private Object data;
}
