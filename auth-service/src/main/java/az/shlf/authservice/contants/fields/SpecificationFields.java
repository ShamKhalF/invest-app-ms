package az.shlf.authservice.contants.fields;

import lombok.Getter;

@Getter
public enum SpecificationFields {

   NAME("name"),
   SURNAME("surname"),
   USERNAME("username"),
   EMAIL("email"),
   STATUS("status");

   private final String value;

   SpecificationFields(String value) {
      this.value = value;
   }

}
