package az.shlf.marketdataservice.exception.dto;

import az.shlf.marketdataservice.exception.dto.ValidationErrorDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionResponse {
   private Integer status;
   private String code;
   private String message;
   private String timestamp;
   private String path;
   private List<ValidationErrorDto> handlers;
}
